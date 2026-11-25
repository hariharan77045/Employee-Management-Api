package com.employeemanagement.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.employeemanagement.entity.PortalEnums.LibraryFormat;
import com.employeemanagement.entity.PortalEnums.Priority;
import com.employeemanagement.entity.PortalEnums.TaskStatus;
import com.employeemanagement.entity.PortalEnums.TicketCategory;
import com.employeemanagement.entity.PortalEnums.TicketStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Shapes for the Documents, Tasks, Library and Help & support pages. */
public final class PortalDtos {

    private PortalDtos() {
    }

    // --- Documents -------------------------------------------------------------

    /** A row in the documents list; the text itself comes from the detail endpoint. */
    public record DocumentSummary(
            Integer id, String title, String category, String description,
            boolean personal, String uploadedBy, LocalDateTime updatedAt) {
    }

    public record DocumentDetail(
            Integer id, String title, String category, String description,
            boolean personal, String uploadedBy, LocalDateTime updatedAt, String content) {
    }

    /** {@code ownerId} empty means a company-wide document. */
    public record DocumentRequest(
            @NotBlank @Size(max = 150) String title,
            @NotBlank @Size(max = 50) String category,
            @Size(max = 300) String description,
            @NotBlank @Size(max = 20000) String content,
            Integer ownerId) {
    }

    // --- Tasks -----------------------------------------------------------------

    public record TaskDto(
            Integer id, String title, String description, Priority priority, TaskStatus status,
            LocalDate dueDate, boolean overdue, LocalDateTime createdAt, LocalDateTime completedAt,
            Integer assigneeId, String assigneeName, Integer createdById, String createdByName) {
    }

    /** {@code assigneeId} empty means "assign to me". */
    public record TaskRequest(
            @NotBlank @Size(max = 150) String title,
            @Size(max = 2000) String description,
            @NotNull Priority priority,
            LocalDate dueDate,
            Integer assigneeId) {
    }

    public record TaskStatusUpdate(@NotNull TaskStatus status) {
    }

    // --- Library ---------------------------------------------------------------

    public record LibraryItemDto(
            Integer id, String title, String author, String category, LibraryFormat format,
            String description, int totalCopies, int available, boolean borrowedByMe) {
    }

    public record LibraryItemRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 150) String author,
            @NotBlank @Size(max = 50) String category,
            @NotNull LibraryFormat format,
            @Size(max = 2000) String description,
            @Min(0) @Max(500) int totalCopies) {
    }

    public record LoanDto(
            Integer id, Integer itemId, String title, String author, LibraryFormat format,
            LocalDate borrowedOn, LocalDate dueOn, LocalDate returnedOn, boolean overdue) {
    }

    // --- Help & support --------------------------------------------------------

    public record FaqDto(Integer id, String category, String question, String answer) {
    }

    public record TicketDto(
            Integer id, Integer employeeId, String employeeName, TicketCategory category,
            String subject, String description, Priority priority, TicketStatus status,
            String resolution, LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record TicketRequest(
            @NotNull TicketCategory category,
            @NotBlank @Size(max = 150) String subject,
            @NotBlank @Size(max = 4000) String description,
            @NotNull Priority priority) {
    }

    public record TicketUpdate(
            @NotNull TicketStatus status,
            @Size(max = 4000) String resolution) {
    }
}
