package com.employeemanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.employeemanagement.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Integer> {

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByEmployeeId(Integer employeeId);

    boolean existsByUsername(String username);

    @Query("select u from AppUser u join fetch u.employee e order by e.firstName, e.lastName, u.id")
    List<AppUser> findAllWithEmployee();

    /** Ids of employees that have no login account yet. */
    @Query("select e.id from Employee e where not exists (select 1 from AppUser u where u.employee = e) order by e.id")
    List<Integer> findEmployeeIdsWithoutAccount();
}
