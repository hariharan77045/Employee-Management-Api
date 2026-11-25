package com.employeemanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "department")
public class Department {

    @Id
    @Column(name = "departmentid")
    private Integer id;

    @Column(name = "departmentname")
    private String name;

    @Column(name = "location")
    private String location;

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
}
