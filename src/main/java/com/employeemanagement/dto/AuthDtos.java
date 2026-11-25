package com.employeemanagement.dto;

import java.time.Instant;
import java.time.LocalDateTime;

import com.employeemanagement.dto.DashboardDtos.CurrentUser;
import com.employeemanagement.entity.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Shapes for /api/auth and /api/admin/users. */
public final class AuthDtos {

    /** 8–64 characters with at least one lower-case letter, one upper-case letter and one digit. */
    public static final String PASSWORD_POLICY = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).{8,64}$";
    public static final String PASSWORD_POLICY_MESSAGE =
            "must be 8-64 characters with an upper-case letter, a lower-case letter and a digit";

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank @Size(max = 100) String username,
            @NotBlank @Size(max = 64) String password) {
    }

    public record LoginResponse(String token, Instant expiresAt, CurrentUser user) {
    }

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Pattern(regexp = PASSWORD_POLICY, message = PASSWORD_POLICY_MESSAGE) String newPassword) {
    }

    /** One row on the admin "User accounts" page. */
    public record UserAccount(
            Integer id,
            Integer employeeId,
            String employeeName,
            String username,
            Role role,
            boolean enabled,
            boolean locked,
            LocalDateTime lastLoginAt) {
    }

    public record UpdateAccountRequest(@NotNull Role role, @NotNull Boolean enabled) {
    }

    /** Shown to the admin once; the employee should change it after signing in. */
    public record PasswordReset(String temporaryPassword) {
    }
}
