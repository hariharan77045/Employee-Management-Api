package com.employeemanagement.entity;

/** Value sets for tasks, support tickets and the library. Stored as text; the tables have matching CHECKs. */
public final class PortalEnums {

    private PortalEnums() {
    }

    public enum Priority { LOW, MEDIUM, HIGH }

    public enum TaskStatus { TODO, IN_PROGRESS, DONE }

    public enum TicketStatus { OPEN, IN_PROGRESS, RESOLVED, CLOSED }

    public enum TicketCategory { IT, HR, PAYROLL, FACILITIES, OTHER }

    public enum LibraryFormat { BOOK, EBOOK, VIDEO, COURSE }
}
