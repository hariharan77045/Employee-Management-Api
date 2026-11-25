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
import jakarta.persistence.Table;

/** One borrowing of a library item. Active while returnedOn is null. */
@Entity
@Table(name = "library_loan")
public class LibraryLoan {

    @Id
    @Column(name = "loanid")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "itemid", nullable = false)
    private LibraryItem item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employeeid", nullable = false)
    private Employee employee;

    @Column(name = "borrowedon", nullable = false)
    private LocalDate borrowedOn;

    @Column(name = "dueon", nullable = false)
    private LocalDate dueOn;

    @Column(name = "returnedon")
    private LocalDate returnedOn;

    public Integer getId() { return id; }
    public LibraryItem getItem() { return item; }
    public void setItem(LibraryItem item) { this.item = item; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public LocalDate getBorrowedOn() { return borrowedOn; }
    public void setBorrowedOn(LocalDate borrowedOn) { this.borrowedOn = borrowedOn; }
    public LocalDate getDueOn() { return dueOn; }
    public void setDueOn(LocalDate dueOn) { this.dueOn = dueOn; }
    public LocalDate getReturnedOn() { return returnedOn; }
    public void setReturnedOn(LocalDate returnedOn) { this.returnedOn = returnedOn; }
}
