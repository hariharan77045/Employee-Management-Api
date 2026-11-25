package com.employeemanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Request and response shapes for the /api/employees endpoints. */
public final class EmployeeDtos {

    private EmployeeDtos() {
    }

    /** One row in the directory list. */
    public record EmployeeSummary(
            Integer id,
            String firstName,
            String lastName,
            String email,
            String phone,
            Integer departmentId,
            String departmentName,
            Integer designationId,
            String designationName,
            LocalDate hireDate) {
    }

    /**
     * Everything the profile page shows about one employee. When {@code restricted} is true the
     * viewer may not see private details (salary, birth date, address, payslips, and for plain
     * employees also leave, reviews and attendance), so those come back null or empty.
     */
    public record EmployeeDetail(
            Integer id,
            String firstName,
            String lastName,
            String gender,
            LocalDate dateOfBirth,
            String email,
            String phone,
            Integer departmentId,
            String departmentName,
            String departmentLocation,
            Integer designationId,
            String designationName,
            String grade,
            BigDecimal salary,
            LocalDate hireDate,
            AddressDto address,
            List<ProjectAssignment> projects,
            List<LeaveDtos.Leave> leaves,
            List<Payslip> payslips,
            List<Review> reviews,
            List<AttendanceEntry> attendance,
            boolean restricted) {
    }

    public record AddressDto(
            @Size(max = 200) String line,
            @Size(max = 100) String city,
            @Size(max = 100) String state,
            @Size(max = 10) String pincode,
            @Size(max = 50) String country) {
    }

    public record ProjectAssignment(
            Integer projectId,
            String name,
            String clientName,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate assignedDate) {
    }

    public record Payslip(
            Integer id,
            String month,
            BigDecimal basicSalary,
            BigDecimal bonus,
            BigDecimal deduction,
            BigDecimal netSalary) {
    }

    public record Review(Integer id, LocalDate reviewDate, Integer rating, String comments) {
    }

    public record AttendanceEntry(LocalDate date, LocalTime checkIn, LocalTime checkOut, String status) {
    }

    /** Body of POST and PUT /api/employees. */
    public record EmployeeRequest(
            @NotBlank @Size(max = 50) String firstName,
            @NotBlank @Size(max = 50) String lastName,
            @NotBlank @Pattern(regexp = "Male|Female|Other", message = "must be Male, Female or Other") String gender,
            @NotNull @Past LocalDate dateOfBirth,
            @NotBlank @Email @Size(max = 100) String email,
            @NotBlank @Pattern(regexp = "\\d{10,15}", message = "must be 10 to 15 digits") String phone,
            @NotNull Integer departmentId,
            @NotNull Integer designationId,
            @NotNull @PositiveOrZero @Digits(integer = 8, fraction = 2) BigDecimal salary,
            @NotNull @PastOrPresent LocalDate hireDate,
            @Valid AddressDto address) {
    }
}
