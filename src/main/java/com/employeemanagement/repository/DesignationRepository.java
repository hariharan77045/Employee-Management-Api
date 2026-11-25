package com.employeemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.employeemanagement.entity.Designation;

public interface DesignationRepository extends JpaRepository<Designation, Integer> {
}
