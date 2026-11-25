package com.employeemanagement.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.employeemanagement.dto.AuthDtos.PasswordReset;
import com.employeemanagement.dto.AuthDtos.UpdateAccountRequest;
import com.employeemanagement.dto.AuthDtos.UserAccount;
import com.employeemanagement.service.AdminUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminUserService service;

    public AdminController(AdminUserService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserAccount> list() {
        return service.list();
    }

    @PutMapping("/{id}")
    public UserAccount update(@PathVariable Integer id, @Valid @RequestBody UpdateAccountRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/unlock")
    public UserAccount unlock(@PathVariable Integer id) {
        return service.unlock(id);
    }

    @PostMapping("/{id}/reset-password")
    public PasswordReset resetPassword(@PathVariable Integer id) {
        return service.resetPassword(id);
    }
}
