package com.employeemanagement.service;

import java.time.temporal.ChronoUnit;

import com.employeemanagement.dto.DashboardDtos;
import com.employeemanagement.dto.EmployeeDtos;
import com.employeemanagement.dto.LeaveDtos;
import com.employeemanagement.entity.Address;
import com.employeemanagement.entity.Announcement;
import com.employeemanagement.entity.Attendance;
import com.employeemanagement.entity.CompanyEvent;
import com.employeemanagement.entity.Department;
import com.employeemanagement.entity.Designation;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.EmployeeProject;
import com.employeemanagement.entity.LeaveRequest;
import com.employeemanagement.entity.Payroll;
import com.employeemanagement.entity.PerformanceReview;

/** Entity -> DTO conversions. Call inside a transaction, since some relations are lazy. */
final class DtoMapper {

    private DtoMapper() {
    }

    static String jobTitle(Employee e) {
        Designation d = e.getDesignation();
        return d == null ? "" : d.getName();
    }

    static EmployeeDtos.EmployeeSummary toSummary(Employee e) {
        Department dept = e.getDepartment();
        Designation desig = e.getDesignation();
        return new EmployeeDtos.EmployeeSummary(
                e.getId(), e.getFirstName(), e.getLastName(), e.getEmail(), e.getPhone(),
                dept == null ? null : dept.getId(), dept == null ? null : dept.getName(),
                desig == null ? null : desig.getId(), desig == null ? null : desig.getName(),
                e.getHireDate());
    }

    static EmployeeDtos.AddressDto toAddress(Address a) {
        return a == null ? null
                : new EmployeeDtos.AddressDto(a.getLine(), a.getCity(), a.getState(), a.getPincode(), a.getCountry());
    }

    static EmployeeDtos.ProjectAssignment toAssignment(EmployeeProject ep) {
        var p = ep.getProject();
        return new EmployeeDtos.ProjectAssignment(p.getId(), p.getName(), p.getClientName(),
                p.getStartDate(), p.getEndDate(), ep.getAssignedDate());
    }

    static EmployeeDtos.Payslip toPayslip(Payroll p) {
        return new EmployeeDtos.Payslip(p.getId(), p.getMonth(), p.getBasicSalary(), p.getBonus(),
                p.getDeduction(), p.getNetSalary());
    }

    static EmployeeDtos.Review toReview(PerformanceReview r) {
        return new EmployeeDtos.Review(r.getId(), r.getReviewDate(), r.getRating(), r.getComments());
    }

    static EmployeeDtos.AttendanceEntry toAttendance(Attendance a) {
        return new EmployeeDtos.AttendanceEntry(a.getDate(), a.getCheckIn(), a.getCheckOut(), a.getStatus());
    }

    static LeaveDtos.Leave toLeave(LeaveRequest l) {
        Employee e = l.getEmployee();
        long days = (l.getFromDate() == null || l.getToDate() == null) ? 0
                : ChronoUnit.DAYS.between(l.getFromDate(), l.getToDate()) + 1;
        return new LeaveDtos.Leave(l.getId(), e.getId(), e.getFullName(), jobTitle(e), l.getLeaveType(),
                l.getFromDate(), l.getToDate(), days, l.getReason(), l.getStatus());
    }

    static DashboardDtos.AnnouncementDto toAnnouncement(Announcement a) {
        return new DashboardDtos.AnnouncementDto(a.getId(), a.getTitle(), a.getSummary(), a.getAuthor(),
                a.getPostedAt());
    }

    static DashboardDtos.EventDto toEvent(CompanyEvent e) {
        return new DashboardDtos.EventDto(e.getId(), e.getTitle(), e.getDescription(), e.getDate(),
                e.getLocation());
    }
}
