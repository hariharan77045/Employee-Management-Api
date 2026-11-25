package com.employeemanagement.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.employeemanagement.dto.LeaveDtos.Leave;
import com.employeemanagement.dto.LeaveDtos.StatusUpdate;
import com.employeemanagement.service.LeaveService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private final LeaveService service;

    public LeaveController(LeaveService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public List<Leave> list(@RequestParam(required = false) String status) {
        return service.list(status);
    }

    @GetMapping("/types")
    public List<String> types() {
        return service.leaveTypes();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public Leave updateStatus(@PathVariable Integer id, @Valid @RequestBody StatusUpdate body) {
        return service.updateStatus(id, body.status());
    }
}
