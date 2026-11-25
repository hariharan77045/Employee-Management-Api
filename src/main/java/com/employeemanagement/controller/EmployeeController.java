package com.employeemanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.employeemanagement.dto.CommonDtos.PageResponse;
import com.employeemanagement.dto.EmployeeDtos.EmployeeDetail;
import com.employeemanagement.dto.EmployeeDtos.EmployeeRequest;
import com.employeemanagement.dto.EmployeeDtos.EmployeeSummary;
import com.employeemanagement.service.EmployeeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<EmployeeSummary> search(@RequestParam(required = false) String search,
                                                @RequestParam(required = false) Integer departmentId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return service.search(search, departmentId, page, size);
    }

    @GetMapping("/{id}")
    public EmployeeDetail get(@PathVariable Integer id) {
        return service.getDetail(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeDetail create(@Valid @RequestBody EmployeeRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public EmployeeDetail update(@PathVariable Integer id, @Valid @RequestBody EmployeeRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}
