package com.employeemanagement.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.employeemanagement.dto.PortalDtos.FaqDto;
import com.employeemanagement.dto.PortalDtos.TicketDto;
import com.employeemanagement.dto.PortalDtos.TicketRequest;
import com.employeemanagement.dto.PortalDtos.TicketUpdate;
import com.employeemanagement.entity.PortalEnums.TicketStatus;
import com.employeemanagement.entity.SupportTicket;
import com.employeemanagement.exception.ApiException;
import com.employeemanagement.repository.FaqRepository;
import com.employeemanagement.repository.SupportTicketRepository;

/** FAQs for everyone; employees raise tickets and HR/admins work the queue. */
@Service
@Transactional(readOnly = true)
public class SupportService {

    private final FaqRepository faqs;
    private final SupportTicketRepository tickets;
    private final CurrentUserService currentUser;

    public SupportService(FaqRepository faqs, SupportTicketRepository tickets, CurrentUserService currentUser) {
        this.faqs = faqs;
        this.tickets = tickets;
        this.currentUser = currentUser;
    }

    public List<FaqDto> faqs() {
        return faqs.findAllByOrderByCategoryAscSortOrderAsc().stream()
                .map(f -> new FaqDto(f.getId(), f.getCategory(), f.getQuestion(), f.getAnswer()))
                .toList();
    }

    public List<TicketDto> myTickets() {
        return tickets.findByEmployee(currentUser.currentEmployeeId()).stream().map(SupportService::toDto).toList();
    }

    public List<TicketDto> queue(TicketStatus status) {
        return tickets.findQueue(status).stream().map(SupportService::toDto).toList();
    }

    @Transactional
    public TicketDto raise(TicketRequest request) {
        SupportTicket t = new SupportTicket();
        t.setEmployee(currentUser.currentEmployee());
        t.setCategory(request.category());
        t.setSubject(request.subject().trim());
        t.setDescription(request.description().trim());
        t.setPriority(request.priority());
        return toDto(tickets.save(t));
    }

    @Transactional
    public TicketDto update(Integer id, TicketUpdate request) {
        SupportTicket t = tickets.findById(id).orElseThrow(() -> ApiException.notFound("Ticket", id));
        boolean closing = request.status() == TicketStatus.RESOLVED || request.status() == TicketStatus.CLOSED;
        String resolution = DocumentService.blankToNull(request.resolution());
        if (closing && resolution == null && t.getResolution() == null) {
            throw ApiException.badRequest("Add a resolution note when resolving or closing a ticket");
        }
        t.setStatus(request.status());
        if (resolution != null) {
            t.setResolution(resolution);
        }
        t.setUpdatedAt(LocalDateTime.now());
        return toDto(t);
    }

    /** Lets the person who raised a ticket close it once they're satisfied. */
    @Transactional
    public TicketDto closeOwn(Integer id) {
        SupportTicket t = tickets.findById(id).orElseThrow(() -> ApiException.notFound("Ticket", id));
        if (t.getEmployee().getId() != currentUser.currentEmployeeId()) {
            throw ApiException.notFound("Ticket", id);
        }
        if (t.getStatus() == TicketStatus.CLOSED) {
            throw ApiException.conflict("This ticket is already closed");
        }
        t.setStatus(TicketStatus.CLOSED);
        if (t.getResolution() == null) {
            t.setResolution("Closed by " + t.getEmployee().getFullName() + ".");
        }
        t.setUpdatedAt(LocalDateTime.now());
        return toDto(t);
    }

    private static TicketDto toDto(SupportTicket t) {
        return new TicketDto(t.getId(), t.getEmployee().getId(), t.getEmployee().getFullName(), t.getCategory(),
                t.getSubject(), t.getDescription(), t.getPriority(), t.getStatus(), t.getResolution(),
                t.getCreatedAt(), t.getUpdatedAt());
    }
}
