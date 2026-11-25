package com.employeemanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.employeemanagement.entity.CompanyDocument;

public interface CompanyDocumentRepository extends JpaRepository<CompanyDocument, Integer> {

    /** Company-wide documents first, then the employee's own, each sorted by category and title. */
    @Query("""
            select d from CompanyDocument d
            left join d.owner o
            where o is null or o.id = :employeeId
            order by case when o is null then 0 else 1 end, d.category, d.title
            """)
    List<CompanyDocument> findVisibleTo(@Param("employeeId") Integer employeeId);
}
