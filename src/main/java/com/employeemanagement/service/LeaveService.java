package com.employeemanagement.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.employeemanagement.dto.EmployeeDtos.Payslip;
import com.employeemanagement.dto.LeaveDtos.ApplyLeaveRequest;
import com.employeemanagement.dto.LeaveDtos.Leave;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.LeaveRequest;
import com.employeemanagement.exception.ApiException;
import com.employeemanagement.repository.LeaveRequestRepository;
import com.employeemanagement.repository.PayrollRepository;

@Service
@Transactional(readOnly = true)
public class LeaveService {

    private static final int MAX_LEAVE_DAYS = 180;

    private final LeaveRequestRepository leaves;
    private final PayrollRepository payroll;
    private final CurrentUserService currentUser;

    public LeaveService(LeaveRequestRepository leaves, PayrollRepository payroll, CurrentUserService currentUser) {
        this.leaves = leaves;
        this.payroll = payroll;
        this.currentUser = currentUser;
    }

    public List<Leave> myLeaves() {
        return leaves.findByEmployeeIdOrderByFromDateDesc(currentUser.currentEmployeeId())
                .stream().map(DtoMapper::toLeave).toList();
    }

    public List<Payslip> myPayslips() {
        return payroll.findByEmployeeIdOrderByIdDesc(currentUser.currentEmployeeId())
                .stream().map(DtoMapper::toPayslip).toList();
    }

    /** All requests, or only those with the given status (Pending, Approved, Rejected). */
    public List<Leave> list(String status) {
        var rows = (status == null || status.isBlank())
                ? leaves.findAllWithEmployee()
                : leaves.findByStatusWithEmployee(status);
        return rows.stream().map(DtoMapper::toLeave).toList();
    }

    public List<String> leaveTypes() {
        return leaves.findDistinctLeaveTypes();
    }

    @Transactional
    public Leave apply(ApplyLeaveRequest request) {
        LocalDate from = request.fromDate();
        LocalDate to = request.toDate();
        if (to.isBefore(from)) {
            throw ApiException.badRequest("The end date can't be before the start date");
        }
        if (from.isBefore(LocalDate.now())) {
            throw ApiException.badRequest("Leave can't start in the past");
        }
        if (from.plusDays(MAX_LEAVE_DAYS).isBefore(to)) {
            throw ApiException.badRequest("A single request can cover at most " + MAX_LEAVE_DAYS + " days");
        }

        Employee me = currentUser.currentEmployee();
        if (leaves.hasActiveLeaveOverlapping(me.getId(), from, to)) {
            throw ApiException.conflict("You already have a pending or approved request that overlaps these dates");
        }

        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(me);
        leave.setLeaveType(request.leaveType().trim());
        leave.setFromDate(from);
        leave.setToDate(to);
        leave.setReason(request.reason() == null || request.reason().isBlank() ? null : request.reason().trim());
        leave.setStatus(LeaveRequest.PENDING);
        return DtoMapper.toLeave(leaves.save(leave));
    }

    @Transactional
    public Leave updateStatus(Integer id, String status) {
        LeaveRequest leave = leaves.findById(id).orElseThrow(() -> ApiException.notFound("Leave request", id));
        if (!LeaveRequest.PENDING.equals(leave.getStatus())) {
            throw ApiException.conflict("This request has already been " + leave.getStatus().toLowerCase());
        }
        if (leave.getEmployee().getId() == currentUser.currentEmployeeId()) {
            throw ApiException.badRequest("You can't approve or reject your own request");
        }
        leave.setStatus(status);
        return DtoMapper.toLeave(leave);
    }

    /** Lets employees withdraw a request that hasn't been decided yet. */
    @Transactional
    public void cancel(Integer id) {
        LeaveRequest leave = leaves.findById(id).orElseThrow(() -> ApiException.notFound("Leave request", id));
        if (leave.getEmployee().getId() != currentUser.currentEmployeeId()) {
            throw ApiException.notFound("Leave request", id);
        }
        if (!LeaveRequest.PENDING.equals(leave.getStatus())) {
            throw ApiException.conflict("Only pending requests can be withdrawn");
        }
        leaves.delete(leave);
    }
}
