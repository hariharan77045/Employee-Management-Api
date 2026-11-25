package com.employeemanagement.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @Column(name = "attendanceid")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employeeid")
    private Employee employee;

    @Column(name = "attendancedate")
    private LocalDate date;

    @Column(name = "checkin")
    private LocalTime checkIn;

    @Column(name = "checkout")
    private LocalTime checkOut;

    @Column(name = "status")
    private String status;

    public Integer getId() { return id; }
    public Employee getEmployee() { return employee; }
    public LocalDate getDate() { return date; }
    public LocalTime getCheckIn() { return checkIn; }
    public LocalTime getCheckOut() { return checkOut; }
    public String getStatus() { return status; }
}
