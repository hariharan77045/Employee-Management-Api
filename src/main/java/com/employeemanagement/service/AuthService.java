package com.employeemanagement.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.employeemanagement.dto.AuthDtos.ChangePasswordRequest;
import com.employeemanagement.dto.AuthDtos.LoginRequest;
import com.employeemanagement.dto.AuthDtos.LoginResponse;
import com.employeemanagement.entity.AppUser;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.Role;
import com.employeemanagement.exception.ApiException;
import com.employeemanagement.repository.AppUserRepository;
import com.employeemanagement.security.JwtTokenService;

@Service
public class AuthService {

    private static final String INVALID_LOGIN = "Incorrect email or password.";

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokens;
    private final CurrentUserService currentUser;
    private final int maxFailedAttempts;
    private final Duration lockDuration;
    private final String defaultPassword;
    /** Compared against when the username is unknown, so both cases take about the same time. */
    private final String dummyHash;

    public AuthService(AppUserRepository users, PasswordEncoder passwordEncoder, JwtTokenService tokens,
                       CurrentUserService currentUser,
                       @Value("${app.security.max-failed-attempts}") int maxFailedAttempts,
                       @Value("${app.security.lock-minutes}") long lockMinutes,
                       @Value("${app.security.default-password}") String defaultPassword) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
        this.currentUser = currentUser;
        this.maxFailedAttempts = maxFailedAttempts;
        this.lockDuration = Duration.ofMinutes(lockMinutes);
        this.defaultPassword = defaultPassword;
        this.dummyHash = passwordEncoder.encode("not-a-real-password");
    }

    /** Failed attempts are saved even though the method throws, hence noRollbackFor. */
    @Transactional(noRollbackFor = ApiException.class)
    public LoginResponse login(LoginRequest request) {
        LocalDateTime now = LocalDateTime.now();
        AppUser user = users.findByUsername(request.username().trim().toLowerCase()).orElse(null);
        if (user == null) {
            passwordEncoder.matches(request.password(), dummyHash);
            throw new ApiException(HttpStatus.UNAUTHORIZED, INVALID_LOGIN);
        }
        if (user.isLocked(now)) {
            long minutes = Math.max(1, Duration.between(now, user.getLockedUntil()).toMinutes() + 1);
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many failed attempts. Try again in " + minutes + " minute" + (minutes == 1 ? "" : "s") + ".");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            user.recordFailedLogin(now, maxFailedAttempts, lockDuration);
            throw new ApiException(HttpStatus.UNAUTHORIZED, INVALID_LOGIN);
        }
        if (!user.isEnabled()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This account is disabled. Contact your administrator.");
        }
        user.recordSuccessfulLogin(now);
        return respond(user);
    }

    /** Changes the password, signs out other sessions and returns a fresh token for this one. */
    @Transactional
    public LoginResponse changePassword(ChangePasswordRequest request) {
        AppUser user = users.findById(currentUser.principal().userId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Please sign in to continue."));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("Your current password is incorrect.");
        }
        if (request.currentPassword().equals(request.newPassword())) {
            throw ApiException.badRequest("Choose a password that's different from your current one.");
        }
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        return respond(user);
    }

    /** Called when HR adds an employee, so they can sign in straight away. */
    @Transactional
    public void createAccountFor(Employee employee) {
        String username = employee.getEmail().trim().toLowerCase();
        if (users.findByEmployeeId(employee.getId()).isPresent() || users.existsByUsername(username)) {
            return;
        }
        AppUser user = new AppUser();
        user.setEmployee(employee);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(defaultPassword));
        user.setRole(Role.EMPLOYEE);
        users.save(user);
    }

    /** Keeps the sign-in name in step when an employee's email changes. */
    @Transactional
    public void syncUsername(Employee employee) {
        users.findByEmployeeId(employee.getId())
                .ifPresent(user -> user.setUsername(employee.getEmail().trim().toLowerCase()));
    }

    private LoginResponse respond(AppUser user) {
        var token = tokens.issue(user);
        return new LoginResponse(token.value(), token.expiresAt(),
                CurrentUserService.toCurrentUser(user.getEmployee(), user.getRole()));
    }
}
