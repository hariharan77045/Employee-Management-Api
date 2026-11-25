package com.employeemanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.employeemanagement.entity.Payroll;

public interface PayrollRepository extends JpaRepository<Payroll, Integer> {

    List<Payroll> findByEmployeeIdOrderByIdDesc(Integer employeeId);

    @Modifying
    @Query("delete from Payroll p where p.employee.id = :employeeId")
    void deleteByEmployeeId(@Param("employeeId") Integer employeeId);
}
