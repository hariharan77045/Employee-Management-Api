package com.employeemanagement.controller;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.employeemanagement.dto.CommonDtos.Lookup;
import com.employeemanagement.repository.DepartmentRepository;
import com.employeemanagement.repository.DesignationRepository;

/** Reference data for drop-downs. */
@RestController
@RequestMapping("/api")
@Transactional(readOnly = true)
public class LookupController {

    private final DepartmentRepository departments;
    private final DesignationRepository designations;

    public LookupController(DepartmentRepository departments, DesignationRepository designations) {
        this.departments = departments;
        this.designations = designations;
    }

    @GetMapping("/departments")
    public List<Lookup> departments() {
        return departments.findAll(Sort.by("id")).stream()
                .map(d -> new Lookup(d.getId(), d.getName(), d.getLocation()))
                .toList();
    }

    @GetMapping("/designations")
    public List<Lookup> designations() {
        return designations.findAll(Sort.by("id")).stream()
                .map(d -> new Lookup(d.getId(), d.getName(), d.getGrade()))
                .toList();
    }
}
