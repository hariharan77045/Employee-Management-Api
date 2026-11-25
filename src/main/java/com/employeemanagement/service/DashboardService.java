package com.employeemanagement.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.employeemanagement.dto.DashboardDtos.Contact;
import com.employeemanagement.dto.DashboardDtos.Course;
import com.employeemanagement.dto.DashboardDtos.Dashboard;
import com.employeemanagement.dto.DashboardDtos.LeaveEntry;
import com.employeemanagement.dto.DashboardDtos.ServiceLink;
import com.employeemanagement.dto.DashboardDtos.Stats;
import com.employeemanagement.dto.DashboardDtos.Weather;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.LeaveRequest;
import com.employeemanagement.repository.AnnouncementRepository;
import com.employeemanagement.repository.CompanyEventRepository;
import com.employeemanagement.repository.DepartmentRepository;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.repository.LeaveRequestRepository;
import com.employeemanagement.repository.ProjectRepository;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    /** How far ahead the "Who's off" widget looks. */
    private static final int LEAVE_WINDOW_DAYS = 14;

    // No tables exist for these yet, so they are fixed content.
    private static final Weather WEATHER = new Weather(30, "Partly cloudy");
    private static final List<String> TIPS = List.of(
            "Grab a cup of coffee and let's get some work done!",
            "Block an hour today for deep, focused work.",
            "Take a short walk between meetings to recharge.",
            "Write down tomorrow's top three priorities before you log off.",
            "Thank a teammate for something they helped you with this week.");
    private static final List<Course> COURSES = List.of(
            new Course(1, "Career Development", 3, "graduation"),
            new Course(2, "Compliance Training", 2, "shield"),
            new Course(3, "Technical Training", 6, "code"),
            new Course(4, "Labs", 1, "flask"));
    private static final List<ServiceLink> SERVICES = List.of(
            new ServiceLink(1, "Directory", "Find a colleague", "folder", "/directory"),
            new ServiceLink(2, "Time off", "Apply for and track leave", "clock", "/my-services"),
            new ServiceLink(3, "Salary slip", "View your payslips", "wallet", "/my-services/payslips"),
            new ServiceLink(4, "My profile", "Your details and history", "user", "/my-profile"));

    private final EmployeeRepository employees;
    private final DepartmentRepository departments;
    private final ProjectRepository projects;
    private final LeaveRequestRepository leaves;
    private final AnnouncementRepository announcements;
    private final CompanyEventRepository events;
    private final CurrentUserService currentUser;

    public DashboardService(EmployeeRepository employees, DepartmentRepository departments,
                            ProjectRepository projects, LeaveRequestRepository leaves,
                            AnnouncementRepository announcements, CompanyEventRepository events,
                            CurrentUserService currentUser) {
        this.employees = employees;
        this.departments = departments;
        this.projects = projects;
        this.leaves = leaves;
        this.announcements = announcements;
        this.events = events;
        this.currentUser = currentUser;
    }

    public Dashboard getDashboard() {
        LocalDate today = LocalDate.now();
        Employee me = currentUser.currentEmployee();

        List<LeaveEntry> whoIsOff = leaves
                .findApprovedOverlapping(today, today.plusDays(LEAVE_WINDOW_DAYS), PageRequest.of(0, 6))
                .stream().map(DashboardService::toLeaveEntry).toList();

        List<Contact> contacts = me.getDepartment() == null ? List.of()
                : employees.findColleagues(me.getDepartment().getId(), me.getId(), PageRequest.of(0, 8))
                        .stream().map(e -> new Contact(e.getId(), e.getFullName(), e.getEmail())).toList();

        Stats stats = new Stats(
                employees.count(),
                departments.count(),
                projects.count(),
                leaves.countByStatus(LeaveRequest.PENDING),
                leaves.countApprovedOn(today));

        return new Dashboard(
                WEATHER,
                TIPS.get(today.getDayOfYear() % TIPS.size()),
                stats,
                announcements.findAllByOrderByPostedAtDesc(PageRequest.of(0, 3))
                        .stream().map(DtoMapper::toAnnouncement).toList(),
                COURSES,
                SERVICES,
                whoIsOff,
                contacts,
                events.findByDateGreaterThanEqualOrderByDateAsc(today, PageRequest.of(0, 5))
                        .stream().map(DtoMapper::toEvent).toList());
    }

    private static LeaveEntry toLeaveEntry(LeaveRequest l) {
        Employee e = l.getEmployee();
        return new LeaveEntry(l.getId(), e.getFullName(), DtoMapper.jobTitle(e), l.getLeaveType(),
                l.getFromDate(), l.getToDate());
    }
}
