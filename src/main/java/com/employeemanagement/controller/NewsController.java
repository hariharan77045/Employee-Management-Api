package com.employeemanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.employeemanagement.dto.DashboardDtos.AnnouncementDto;
import com.employeemanagement.dto.DashboardDtos.AnnouncementRequest;
import com.employeemanagement.dto.DashboardDtos.EventDto;
import com.employeemanagement.dto.DashboardDtos.EventRequest;
import com.employeemanagement.service.NewsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class NewsController {

    private final NewsService service;

    public NewsController(NewsService service) {
        this.service = service;
    }

    @GetMapping("/announcements")
    public List<AnnouncementDto> announcements() {
        return service.announcements();
    }

    @PostMapping("/announcements")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @ResponseStatus(HttpStatus.CREATED)
    public AnnouncementDto postAnnouncement(@Valid @RequestBody AnnouncementRequest request) {
        return service.postAnnouncement(request);
    }

    @DeleteMapping("/announcements/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAnnouncement(@PathVariable Integer id) {
        service.deleteAnnouncement(id);
    }

    @GetMapping("/events")
    public List<EventDto> events() {
        return service.events();
    }

    @PostMapping("/events")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto createEvent(@Valid @RequestBody EventRequest request) {
        return service.createEvent(request);
    }

    @DeleteMapping("/events/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Integer id) {
        service.deleteEvent(id);
    }
}
