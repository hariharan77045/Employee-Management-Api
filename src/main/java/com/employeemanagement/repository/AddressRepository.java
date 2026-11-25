package com.employeemanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.employeemanagement.entity.Address;

public interface AddressRepository extends JpaRepository<Address, Integer> {

    Optional<Address> findFirstByEmployeeIdOrderByIdAsc(Integer employeeId);

    @Modifying
    @Query("delete from Address a where a.employee.id = :employeeId")
    void deleteByEmployeeId(@Param("employeeId") Integer employeeId);
}
