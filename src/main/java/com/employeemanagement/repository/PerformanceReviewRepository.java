package com.employeemanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.employeemanagement.entity.PerformanceReview;

public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Integer> {

    List<PerformanceReview> findByEmployeeIdOrderByReviewDateDesc(Integer employeeId);

    @Modifying
    @Query("delete from PerformanceReview r where r.employee.id = :employeeId")
    void deleteByEmployeeId(@Param("employeeId") Integer employeeId);
}
