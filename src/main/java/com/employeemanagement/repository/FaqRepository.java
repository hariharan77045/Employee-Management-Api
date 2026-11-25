package com.employeemanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.employeemanagement.entity.Faq;

public interface FaqRepository extends JpaRepository<Faq, Integer> {

    List<Faq> findAllByOrderByCategoryAscSortOrderAsc();
}
