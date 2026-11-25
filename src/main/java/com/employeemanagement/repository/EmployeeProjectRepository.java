package com.employeemanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.employeemanagement.entity.EmployeeProject;

public interface EmployeeProjectRepository extends JpaRepository<EmployeeProject, Integer> {

    @Query("""
            select ep from EmployeeProject ep
            join fetch ep.project
            where ep.employee.id = :employeeId
            order by ep.assignedDate desc
            """)
    List<EmployeeProject> findByEmployeeIdWithProject(@Param("employeeId") Integer employeeId);

    @Modifying
    @Query("delete from EmployeeProject ep where ep.employee.id = :employeeId")
    void deleteByEmployeeId(@Param("employeeId") Integer employeeId);
}
