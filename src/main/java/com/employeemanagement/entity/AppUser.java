package com.employeemanagement.entity;

import java.time.Duration;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/** A login account. Each employee has at most one; the username is the employee's email. */
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @Column(name = "userid")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employeeid", nullable = false, unique = true)
    private Employee employee;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "passwordhash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.EMPLOYEE;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "failedattempts", nullable = false)
    private int failedAttempts;

    @Column(name = "lockeduntil")
    private LocalDateTime lockedUntil;

    /** Part of every token; bumping it signs the account out everywhere. */
    @Column(name = "tokenversion", nullable = false)
    private int tokenVersion;

    @Column(name = "lastloginat")
    private LocalDateTime lastLoginAt;

    @Column(name = "createdat", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public boolean isLocked(LocalDateTime now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    /** Counts a wrong password and locks the account once {@code maxAttempts} is reached. */
    public void recordFailedLogin(LocalDateTime now, int maxAttempts, Duration lockFor) {
        failedAttempts++;
        if (failedAttempts >= maxAttempts) {
            lockedUntil = now.plus(lockFor);
            failedAttempts = 0;
        }
    }

    public void recordSuccessfulLogin(LocalDateTime now) {
        failedAttempts = 0;
        lockedUntil = null;
        lastLoginAt = now;
    }

    public void unlock() {
        failedAttempts = 0;
        lockedUntil = null;
    }

    public void changePassword(String newHash) {
        passwordHash = newHash;
        tokenVersion++;
    }

    public Integer getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
