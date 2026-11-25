package com.employeemanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.employeemanagement.dto.PortalDtos.LibraryItemDto;
import com.employeemanagement.dto.PortalDtos.LibraryItemRequest;
import com.employeemanagement.dto.PortalDtos.LoanDto;
import com.employeemanagement.service.LibraryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/library")
public class LibraryController {

    private final LibraryService service;

    public LibraryController(LibraryService service) {
        this.service = service;
    }

    @GetMapping("/items")
    public List<LibraryItemDto> items(@RequestParam(required = false) String search,
                                      @RequestParam(required = false) String category) {
        return service.search(search, category);
    }

    @GetMapping("/categories")
    public List<String> categories() {
        return service.categories();
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public LibraryItemDto addItem(@Valid @RequestBody LibraryItemRequest request) {
        return service.addItem(request);
    }

    @GetMapping("/loans/mine")
    public List<LoanDto> myLoans() {
        return service.myLoans();
    }

    @PostMapping("/items/{id}/borrow")
    @ResponseStatus(HttpStatus.CREATED)
    public LoanDto borrow(@PathVariable Integer id) {
        return service.borrow(id);
    }

    @PostMapping("/loans/{id}/return")
    public LoanDto giveBack(@PathVariable Integer id) {
        return service.giveBack(id);
    }
}
