package com.employeemanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.employeemanagement.entity.PortalEnums.TicketStatus;
import com.employeemanagement.entity.SupportTicket;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Integer> {

    @Query("""
            select t from SupportTicket t
            join fetch t.employee
            where t.employee.id = :employeeId
            order by t.createdAt desc
            """)
    List<SupportTicket> findByEmployee(@Param("employeeId") Integer employeeId);

    /** The support queue; a null status means every ticket. */
    @Query("""
            select t from SupportTicket t
            join fetch t.employee
            where :status is null or t.status = :status
            order by t.createdAt desc
            """)
    List<SupportTicket> findQueue(@Param("status") TicketStatus status);
}
