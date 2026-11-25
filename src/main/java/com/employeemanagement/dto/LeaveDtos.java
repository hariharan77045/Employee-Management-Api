package com.employeemanagement.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Request and response shapes for leave requests. */
public final class LeaveDtos {

    private LeaveDtos() {
    }

    public record Leave(
            Integer id,
            Integer employeeId,
            String employeeName,
            String jobTitle,
            String leaveType,
            LocalDate fromDate,
            LocalDate toDate,
            long days,
            String reason,
            String status) {
    }

    /** Body of POST /api/me/leaves. */
    public record ApplyLeaveRequest(
            @NotBlank @Size(max = 50) String leaveType,
            @NotNull LocalDate fromDate,
            @NotNull LocalDate toDate,
            @Size(max = 500) String reason) {
    }

    /** Body of PATCH /api/leaves/{id}/status. */
    public record StatusUpdate(
            @NotBlank @Pattern(regexp = "Approved|Rejected", message = "must be Approved or Rejected") String status) {
    }
}
