package com.employeemanagement.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.employeemanagement.entity.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Shapes used by the dashboard and News & events pages. They match the Angular models. */
public final class DashboardDtos {

    private DashboardDtos() {
    }

    public record CurrentUser(
            Integer id,
            String firstName,
            String lastName,
            String jobTitle,
            String email,
            String departmentName,
            Role role) {
    }

    public record Dashboard(
            Weather weather,
            String tip,
            Stats stats,
            List<AnnouncementDto> announcements,
            List<Course> courses,
            List<ServiceLink> services,
            List<LeaveEntry> leaves,
            List<Contact> contacts,
            List<EventDto> events) {
    }

    public record Weather(int temperatureC, String condition) {
    }

    public record Stats(long employees, long departments, long projects, long pendingLeaves, long onLeaveToday) {
    }

    public record Course(int id, String title, int durationMonths, String icon) {
    }

    public record ServiceLink(int id, String title, String description, String icon, String route) {
    }

    public record LeaveEntry(
            Integer id,
            String employeeName,
            String jobTitle,
            String leaveType,
            LocalDate fromDate,
            LocalDate toDate) {
    }

    public record Contact(Integer id, String name, String email) {
    }

    public record AnnouncementDto(Integer id, String title, String summary, String author, LocalDateTime postedAt) {
    }

    public record EventDto(Integer id, String title, String description, LocalDate date, String location) {
    }

    public record AnnouncementRequest(
            @NotBlank @Size(max = 150) String title,
            @NotBlank @Size(max = 2000) String summary) {
    }

    public record EventRequest(
            @NotBlank @Size(max = 150) String title,
            @Size(max = 2000) String description,
            @NotNull LocalDate date,
            @Size(max = 100) String location) {
    }
}
