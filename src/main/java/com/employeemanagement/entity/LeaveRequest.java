package com.employeemanagement.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "leaverequest")
public class LeaveRequest {

    public static final String PENDING = "Pending";
    public static final String APPROVED = "Approved";
    public static final String REJECTED = "Rejected";

    @Id
    @Column(name = "leaveid")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "leaverequest_seq")
    @SequenceGenerator(name = "leaverequest_seq", sequenceName = "leaverequest_seq", allocationSize = 1)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employeeid")
    private Employee employee;

    @Column(name = "leavetype")
    private String leaveType;

    @Column(name = "fromdate")
    private LocalDate fromDate;

    @Column(name = "todate")
    private LocalDate toDate;

    @Column(name = "reason", columnDefinition = "text")
    private String reason;

    @Column(name = "status")
    private String status;

    public Integer getId() { return id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
