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
@Table(name = "performancereview")
public class PerformanceReview {

    @Id
    @Column(name = "reviewid")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employeeid")
    private Employee employee;

    @Column(name = "reviewdate")
    private LocalDate reviewDate;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "comments", columnDefinition = "text")
    private String comments;

    public Integer getId() { return id; }
    public Employee getEmployee() { return employee; }
    public LocalDate getReviewDate() { return reviewDate; }
    public Integer getRating() { return rating; }
    public String getComments() { return comments; }
}
