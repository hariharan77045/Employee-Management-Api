package com.employeemanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.employeemanagement.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    /** Tasks assigned to the employee, with both people loaded for display. */
    @Query("""
            select t from Task t
            join fetch t.assignee
            left join fetch t.createdBy
            where t.assignee.id = :employeeId
            order by t.dueDate asc nulls last, t.id
            """)
    List<Task> findAssignedTo(@Param("employeeId") Integer employeeId);

    /** Tasks the employee created for other people. */
    @Query("""
            select t from Task t
            join fetch t.assignee a
            left join fetch t.createdBy c
            where c.id = :employeeId and a.id <> :employeeId
            order by t.dueDate asc nulls last, t.id
            """)
    List<Task> findCreatedForOthers(@Param("employeeId") Integer employeeId);
}
