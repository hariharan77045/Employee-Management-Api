package com.employeemanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.employeemanagement.entity.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

    List<Attendance> findTop10ByEmployeeIdOrderByDateDesc(Integer employeeId);

    @Modifying
    @Query("delete from Attendance a where a.employee.id = :employeeId")
    void deleteByEmployeeId(@Param("employeeId") Integer employeeId);
}
