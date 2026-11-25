package com.employeemanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "designation")
public class Designation {

    @Id
    @Column(name = "designationid")
    private Integer id;

    @Column(name = "designationname")
    private String name;

    @Column(name = "grade")
    private String grade;

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getGrade() { return grade; }
}
