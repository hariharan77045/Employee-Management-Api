package com.employeemanagement.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.employeemanagement.dto.AuthDtos.PasswordReset;
import com.employeemanagement.dto.AuthDtos.UpdateAccountRequest;
import com.employeemanagement.dto.AuthDtos.UserAccount;
import com.employeemanagement.entity.AppUser;
import com.employeemanagement.exception.ApiException;
import com.employeemanagement.repository.AppUserRepository;

/** Account management for the admin "User accounts" page. */
@Service
@Transactional(readOnly = true)
public class AdminUserService {

    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnpqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUser;

    public AdminUserService(AppUserRepository users, PasswordEncoder passwordEncoder, CurrentUserService currentUser) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.currentUser = currentUser;
    }

    public List<UserAccount> list() {
        LocalDateTime now = LocalDateTime.now();
        return users.findAllWithEmployee().stream().map(u -> toDto(u, now)).toList();
    }

    @Transactional
    public UserAccount update(Integer id, UpdateAccountRequest request) {
        AppUser user = find(id);
        if (user.getId().equals(currentUser.principal().userId())
                && (request.role() != user.getRole() || !request.enabled())) {
            throw ApiException.badRequest("You can't change your own role or disable your own account");
        }
        user.setRole(request.role());
        user.setEnabled(request.enabled());
        return toDto(user, LocalDateTime.now());
    }

    @Transactional
    public UserAccount unlock(Integer id) {
        AppUser user = find(id);
        user.unlock();
        return toDto(user, LocalDateTime.now());
    }

    /** Sets a random temporary password and signs the account out everywhere. */
    @Transactional
    public PasswordReset resetPassword(Integer id) {
        AppUser user = find(id);
        String temporary = temporaryPassword();
        user.changePassword(passwordEncoder.encode(temporary));
        user.unlock();
        return new PasswordReset(temporary);
    }

    private AppUser find(Integer id) {
        return users.findById(id).orElseThrow(() -> ApiException.notFound("User account", id));
    }

    private static UserAccount toDto(AppUser u, LocalDateTime now) {
        return new UserAccount(u.getId(), u.getEmployee().getId(), u.getEmployee().getFullName(), u.getUsername(),
                u.getRole(), u.isEnabled(), u.isLocked(now), u.getLastLoginAt());
    }

    /** 12 characters that always satisfy the password policy. */
    private static String temporaryPassword() {
        String all = UPPER + LOWER + DIGITS;
        StringBuilder sb = new StringBuilder();
        sb.append(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        sb.append(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        sb.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        for (int i = 0; i < 9; i++) {
            sb.append(all.charAt(RANDOM.nextInt(all.length())));
        }
        return sb.toString();
    }
}
