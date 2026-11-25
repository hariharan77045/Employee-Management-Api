package com.employeemanagement.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "project")
public class Project {

    @Id
    @Column(name = "projectid")
    private Integer id;

    @Column(name = "projectname")
    private String name;

    @Column(name = "clientname")
    private String clientName;

    @Column(name = "startdate")
    private LocalDate startDate;

    @Column(name = "enddate")
    private LocalDate endDate;

    @Column(name = "budget")
    private BigDecimal budget;

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getClientName() { return clientName; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public BigDecimal getBudget() { return budget; }
}
