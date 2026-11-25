package com.employeemanagement.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.employeemanagement.entity.CompanyEvent;

public interface CompanyEventRepository extends JpaRepository<CompanyEvent, Integer> {

    List<CompanyEvent> findAllByOrderByDateAsc();

    List<CompanyEvent> findByDateGreaterThanEqualOrderByDateAsc(LocalDate from, Pageable pageable);
}
