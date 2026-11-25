package com.employeemanagement.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "employeeproject")
public class EmployeeProject {

    @Id
    @Column(name = "employeeprojectid")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employeeid")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectid")
    private Project project;

    @Column(name = "assigneddate")
    private LocalDate assignedDate;

    public Integer getId() { return id; }
    public Employee getEmployee() { return employee; }
    public Project getProject() { return project; }
    public LocalDate getAssignedDate() { return assignedDate; }
}
