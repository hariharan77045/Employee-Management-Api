package com.employeemanagement.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.employeemanagement.entity.LeaveRequest;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer> {

    List<LeaveRequest> findByEmployeeIdOrderByFromDateDesc(Integer employeeId);

    @Query("""
            select l from LeaveRequest l
            join fetch l.employee e
            left join fetch e.designation
            where l.status = :status
            order by l.fromDate desc
            """)
    List<LeaveRequest> findByStatusWithEmployee(@Param("status") String status);

    @Query("""
            select l from LeaveRequest l
            join fetch l.employee e
            left join fetch e.designation
            order by l.fromDate desc
            """)
    List<LeaveRequest> findAllWithEmployee();

    /** Approved leave that overlaps the [start, end] window. */
    @Query("""
            select l from LeaveRequest l
            join fetch l.employee e
            left join fetch e.designation
            where l.status = 'Approved' and l.fromDate <= :end and l.toDate >= :start
            order by l.fromDate
            """)
    List<LeaveRequest> findApprovedOverlapping(@Param("start") LocalDate start,
                                               @Param("end") LocalDate end,
                                               Pageable pageable);

    @Query("""
            select count(l) > 0 from LeaveRequest l
            where l.employee.id = :employeeId
              and l.status in ('Pending', 'Approved')
              and l.fromDate <= :end and l.toDate >= :start
            """)
    boolean hasActiveLeaveOverlapping(@Param("employeeId") Integer employeeId,
                                      @Param("start") LocalDate start,
                                      @Param("end") LocalDate end);

    long countByStatus(String status);

    @Query("""
            select count(l) from LeaveRequest l
            where l.status = 'Approved' and l.fromDate <= :day and l.toDate >= :day
            """)
    long countApprovedOn(@Param("day") LocalDate day);

    @Query("select distinct l.leaveType from LeaveRequest l where l.leaveType is not null order by l.leaveType")
    List<String> findDistinctLeaveTypes();

    @Modifying
    @Query("delete from LeaveRequest l where l.employee.id = :employeeId")
    void deleteByEmployeeId(@Param("employeeId") Integer employeeId);
}
