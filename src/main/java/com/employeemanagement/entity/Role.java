package com.employeemanagement.entity;

/**
 * What an account may do. Each role includes everything the roles below it can do:
 * ADMIN (manage accounts) > HR (manage employees, news, salaries) > MANAGER (decide leave) > EMPLOYEE.
 */
public enum Role {
    ADMIN, HR, MANAGER, EMPLOYEE
}
