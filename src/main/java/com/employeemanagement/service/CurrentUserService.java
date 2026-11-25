package com.employeemanagement.service;

import java.util.Arrays;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.employeemanagement.dto.DashboardDtos.CurrentUser;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.Role;
import com.employeemanagement.exception.ApiException;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.security.AccountPrincipal;

/** Resolves the signed-in employee from the bearer token (see AccountJwtConverter). */
@Service
@Transactional(readOnly = true)
public class CurrentUserService {

    private final EmployeeRepository employees;

    public CurrentUserService(EmployeeRepository employees) {
        this.employees = employees;
    }

    public AccountPrincipal principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AccountPrincipal principal) {
            return principal;
        }
        throw new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Please sign in to continue.");
    }

    public int currentEmployeeId() {
        return principal().employeeId();
    }

    public boolean hasAnyRole(Role... roles) {
        Role role = principal().role();
        return Arrays.asList(roles).contains(role);
    }

    public Employee currentEmployee() {
        int id = currentEmployeeId();
        return employees.findWithDetailsById(id).orElseThrow(() -> ApiException.notFound("Employee", id));
    }

    public CurrentUser currentUser() {
        return toCurrentUser(currentEmployee(), principal().role());
    }

    public static CurrentUser toCurrentUser(Employee e, Role role) {
        return new CurrentUser(e.getId(), e.getFirstName(), e.getLastName(), DtoMapper.jobTitle(e), e.getEmail(),
                e.getDepartment() == null ? null : e.getDepartment().getName(), role);
    }
}
