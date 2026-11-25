package com.employeemanagement.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.employeemanagement.dto.DashboardDtos.AnnouncementDto;
import com.employeemanagement.dto.DashboardDtos.AnnouncementRequest;
import com.employeemanagement.dto.DashboardDtos.EventDto;
import com.employeemanagement.dto.DashboardDtos.EventRequest;
import com.employeemanagement.entity.Announcement;
import com.employeemanagement.entity.CompanyEvent;
import com.employeemanagement.exception.ApiException;
import com.employeemanagement.repository.AnnouncementRepository;
import com.employeemanagement.repository.CompanyEventRepository;

@Service
@Transactional(readOnly = true)
public class NewsService {

    private final AnnouncementRepository announcements;
    private final CompanyEventRepository events;
    private final CurrentUserService currentUser;

    public NewsService(AnnouncementRepository announcements, CompanyEventRepository events,
                       CurrentUserService currentUser) {
        this.announcements = announcements;
        this.events = events;
        this.currentUser = currentUser;
    }

    public List<AnnouncementDto> announcements() {
        return announcements.findAllByOrderByPostedAtDesc().stream().map(DtoMapper::toAnnouncement).toList();
    }

    public List<EventDto> events() {
        return events.findAllByOrderByDateAsc().stream().map(DtoMapper::toEvent).toList();
    }

    @Transactional
    public AnnouncementDto postAnnouncement(AnnouncementRequest request) {
        Announcement a = new Announcement();
        a.setTitle(request.title().trim());
        a.setSummary(request.summary().trim());
        a.setAuthor(currentUser.currentEmployee().getFullName());
        a.setPostedAt(LocalDateTime.now());
        return DtoMapper.toAnnouncement(announcements.save(a));
    }

    @Transactional
    public EventDto createEvent(EventRequest request) {
        CompanyEvent e = new CompanyEvent();
        e.setTitle(request.title().trim());
        e.setDescription(request.description() == null || request.description().isBlank()
                ? null : request.description().trim());
        e.setDate(request.date());
        e.setLocation(request.location() == null || request.location().isBlank() ? null : request.location().trim());
        return DtoMapper.toEvent(events.save(e));
    }

    @Transactional
    public void deleteAnnouncement(Integer id) {
        if (!announcements.existsById(id)) {
            throw ApiException.notFound("Announcement", id);
        }
        announcements.deleteById(id);
    }

    @Transactional
    public void deleteEvent(Integer id) {
        if (!events.existsById(id)) {
            throw ApiException.notFound("Event", id);
        }
        events.deleteById(id);
    }
}
