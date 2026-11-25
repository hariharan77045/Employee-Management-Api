package com.employeemanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.employeemanagement.dto.PortalDtos.FaqDto;
import com.employeemanagement.dto.PortalDtos.TicketDto;
import com.employeemanagement.dto.PortalDtos.TicketRequest;
import com.employeemanagement.dto.PortalDtos.TicketUpdate;
import com.employeemanagement.entity.PortalEnums.TicketStatus;
import com.employeemanagement.service.SupportService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/support")
public class SupportController {

    private final SupportService service;

    public SupportController(SupportService service) {
        this.service = service;
    }

    @GetMapping("/faqs")
    public List<FaqDto> faqs() {
        return service.faqs();
    }

    @GetMapping("/tickets/mine")
    public List<TicketDto> myTickets() {
        return service.myTickets();
    }

    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketDto raise(@Valid @RequestBody TicketRequest request) {
        return service.raise(request);
    }

    @PostMapping("/tickets/{id}/close")
    public TicketDto closeOwn(@PathVariable Integer id) {
        return service.closeOwn(id);
    }

    @GetMapping("/tickets")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public List<TicketDto> queue(@RequestParam(required = false) TicketStatus status) {
        return service.queue(status);
    }

    @PatchMapping("/tickets/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public TicketDto update(@PathVariable Integer id, @Valid @RequestBody TicketUpdate request) {
        return service.update(id, request);
    }
}
