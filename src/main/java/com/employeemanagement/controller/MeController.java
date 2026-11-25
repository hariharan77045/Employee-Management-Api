package com.employeemanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.employeemanagement.dto.DashboardDtos.CurrentUser;
import com.employeemanagement.dto.DashboardDtos.Dashboard;
import com.employeemanagement.dto.EmployeeDtos.Payslip;
import com.employeemanagement.dto.LeaveDtos.ApplyLeaveRequest;
import com.employeemanagement.dto.LeaveDtos.Leave;
import com.employeemanagement.service.CurrentUserService;
import com.employeemanagement.service.DashboardService;
import com.employeemanagement.service.LeaveService;

import jakarta.validation.Valid;

/** Endpoints scoped to the current ("logged in") employee. */
@RestController
@RequestMapping("/api")
public class MeController {

    private final CurrentUserService currentUser;
    private final DashboardService dashboard;
    private final LeaveService leaves;

    public MeController(CurrentUserService currentUser, DashboardService dashboard, LeaveService leaves) {
        this.currentUser = currentUser;
        this.dashboard = dashboard;
        this.leaves = leaves;
    }

    @GetMapping("/me")
    public CurrentUser me() {
        return currentUser.currentUser();
    }

    @GetMapping("/dashboard")
    public Dashboard dashboard() {
        return dashboard.getDashboard();
    }

    @GetMapping("/me/leaves")
    public List<Leave> myLeaves() {
        return leaves.myLeaves();
    }

    @PostMapping("/me/leaves")
    @ResponseStatus(HttpStatus.CREATED)
    public Leave applyLeave(@Valid @RequestBody ApplyLeaveRequest request) {
        return leaves.apply(request);
    }

    @DeleteMapping("/me/leaves/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdrawLeave(@PathVariable Integer id) {
        leaves.cancel(id);
    }

    @GetMapping("/me/payslips")
    public List<Payslip> myPayslips() {
        return leaves.myPayslips();
    }
}
