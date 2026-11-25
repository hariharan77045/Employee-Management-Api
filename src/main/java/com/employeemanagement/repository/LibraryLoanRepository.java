package com.employeemanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.employeemanagement.entity.LibraryLoan;

public interface LibraryLoanRepository extends JpaRepository<LibraryLoan, Integer> {

    /** Rows of [itemId, active loan count] for every item that is currently out. */
    @Query("select l.item.id, count(l) from LibraryLoan l where l.returnedOn is null group by l.item.id")
    List<Object[]> countActiveLoansByItem();

    long countByItemIdAndReturnedOnIsNull(Integer itemId);

    long countByEmployeeIdAndReturnedOnIsNull(Integer employeeId);

    boolean existsByItemIdAndEmployeeIdAndReturnedOnIsNull(Integer itemId, Integer employeeId);

    @Query("select l.item.id from LibraryLoan l where l.employee.id = :employeeId and l.returnedOn is null")
    List<Integer> findActiveItemIds(@Param("employeeId") Integer employeeId);

    /** The employee's loans: active ones first (soonest due), then returned ones (latest first). */
    @Query("""
            select l from LibraryLoan l
            join fetch l.item
            where l.employee.id = :employeeId
            order by case when l.returnedOn is null then 0 else 1 end, l.dueOn, l.returnedOn desc
            """)
    List<LibraryLoan> findByEmployeeWithItem(@Param("employeeId") Integer employeeId);
}
