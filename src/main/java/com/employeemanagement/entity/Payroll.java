package com.employeemanagement.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "payroll")
public class Payroll {

    @Id
    @Column(name = "payrollid")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employeeid")
    private Employee employee;

    @Column(name = "month")
    private String month;

    @Column(name = "basicsalary")
    private BigDecimal basicSalary;

    @Column(name = "bonus")
    private BigDecimal bonus;

    @Column(name = "deduction")
    private BigDecimal deduction;

    @Column(name = "netsalary")
    private BigDecimal netSalary;

    public Integer getId() { return id; }
    public Employee getEmployee() { return employee; }
    public String getMonth() { return month; }
    public BigDecimal getBasicSalary() { return basicSalary; }
    public BigDecimal getBonus() { return bonus; }
    public BigDecimal getDeduction() { return deduction; }
    public BigDecimal getNetSalary() { return netSalary; }
}
