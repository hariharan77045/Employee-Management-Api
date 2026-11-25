package com.employeemanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.employeemanagement.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    /**
     * Directory search. {@code pattern} is a lower-case LIKE pattern ("%" matches everyone);
     * {@code departmentId} 0 means "all departments".
     */
    @Query(value = """
            select e from Employee e
            left join fetch e.department d
            left join fetch e.designation
            where (:departmentId = 0 or d.id = :departmentId)
              and (lower(concat(e.firstName, ' ', e.lastName)) like :pattern
                   or lower(e.email) like :pattern
                   or e.phone like :pattern)
            """,
            countQuery = """
            select count(e) from Employee e
            left join e.department d
            where (:departmentId = 0 or d.id = :departmentId)
              and (lower(concat(e.firstName, ' ', e.lastName)) like :pattern
                   or lower(e.email) like :pattern
                   or e.phone like :pattern)
            """)
    Page<Employee> search(@Param("pattern") String pattern, @Param("departmentId") int departmentId, Pageable pageable);

    @Query("""
            select e from Employee e
            left join fetch e.department
            left join fetch e.designation
            where e.id = :id
            """)
    Optional<Employee> findWithDetailsById(@Param("id") Integer id);

    @Query("""
            select e from Employee e
            where e.department.id = :departmentId and e.id <> :excludeId
            order by e.firstName, e.lastName
            """)
    List<Employee> findColleagues(@Param("departmentId") Integer departmentId,
                                  @Param("excludeId") Integer excludeId,
                                  Pageable pageable);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Integer id);
}
