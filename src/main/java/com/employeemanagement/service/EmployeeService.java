package com.employeemanagement.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.employeemanagement.dto.CommonDtos.PageResponse;
import com.employeemanagement.dto.EmployeeDtos.AddressDto;
import com.employeemanagement.dto.EmployeeDtos.EmployeeDetail;
import com.employeemanagement.dto.EmployeeDtos.EmployeeRequest;
import com.employeemanagement.dto.EmployeeDtos.EmployeeSummary;
import com.employeemanagement.entity.Address;
import com.employeemanagement.entity.Department;
import com.employeemanagement.entity.Designation;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.Role;
import com.employeemanagement.exception.ApiException;
import com.employeemanagement.repository.AddressRepository;
import com.employeemanagement.repository.AttendanceRepository;
import com.employeemanagement.repository.DepartmentRepository;
import com.employeemanagement.repository.DesignationRepository;
import com.employeemanagement.repository.EmployeeProjectRepository;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.repository.LeaveRequestRepository;
import com.employeemanagement.repository.PayrollRepository;
import com.employeemanagement.repository.PerformanceReviewRepository;

@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private static final int MAX_PAGE_SIZE = 100;

    private final EmployeeRepository employees;
    private final DepartmentRepository departments;
    private final DesignationRepository designations;
    private final AddressRepository addresses;
    private final AttendanceRepository attendance;
    private final EmployeeProjectRepository employeeProjects;
    private final LeaveRequestRepository leaves;
    private final PayrollRepository payroll;
    private final PerformanceReviewRepository reviews;
    private final CurrentUserService currentUser;
    private final AuthService auth;

    public EmployeeService(EmployeeRepository employees, DepartmentRepository departments,
                           DesignationRepository designations, AddressRepository addresses,
                           AttendanceRepository attendance, EmployeeProjectRepository employeeProjects,
                           LeaveRequestRepository leaves, PayrollRepository payroll,
                           PerformanceReviewRepository reviews, CurrentUserService currentUser,
                           AuthService auth) {
        this.auth = auth;
        this.employees = employees;
        this.departments = departments;
        this.designations = designations;
        this.addresses = addresses;
        this.attendance = attendance;
        this.employeeProjects = employeeProjects;
        this.leaves = leaves;
        this.payroll = payroll;
        this.reviews = reviews;
        this.currentUser = currentUser;
    }

    public PageResponse<EmployeeSummary> search(String query, Integer departmentId, int page, int size) {
        String term = query == null ? "" : query.trim().toLowerCase();
        String pattern = "%" + term.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by("firstName", "lastName", "id"));

        return PageResponse.of(employees.search(pattern, departmentId == null ? 0 : departmentId, pageable)
                .map(DtoMapper::toSummary));
    }

    /**
     * Your own profile, or anyone's for HR and admins, shows everything. Managers also see
     * colleagues' leave, reviews and attendance; other employees see only work contact details.
     */
    public EmployeeDetail getDetail(Integer id) {
        Employee e = findEmployee(id);
        var dept = e.getDepartment();
        var desig = e.getDesignation();
        boolean self = id == currentUser.currentEmployeeId();
        boolean full = self || currentUser.hasAnyRole(Role.ADMIN, Role.HR);
        boolean work = full || currentUser.hasAnyRole(Role.MANAGER);

        return new EmployeeDetail(
                e.getId(), e.getFirstName(), e.getLastName(), e.getGender(),
                full ? e.getDateOfBirth() : null,
                e.getEmail(), e.getPhone(),
                dept == null ? null : dept.getId(), dept == null ? null : dept.getName(),
                dept == null ? null : dept.getLocation(),
                desig == null ? null : desig.getId(), desig == null ? null : desig.getName(),
                desig == null ? null : desig.getGrade(),
                full ? e.getSalary() : null, e.getHireDate(),
                full ? addresses.findFirstByEmployeeIdOrderByIdAsc(id).map(DtoMapper::toAddress).orElse(null) : null,
                employeeProjects.findByEmployeeIdWithProject(id).stream().map(DtoMapper::toAssignment).toList(),
                work ? leaves.findByEmployeeIdOrderByFromDateDesc(id).stream().map(DtoMapper::toLeave).toList() : List.of(),
                full ? payroll.findByEmployeeIdOrderByIdDesc(id).stream().map(DtoMapper::toPayslip).toList() : List.of(),
                work ? reviews.findByEmployeeIdOrderByReviewDateDesc(id).stream().map(DtoMapper::toReview).toList() : List.of(),
                work ? attendance.findTop10ByEmployeeIdOrderByDateDesc(id).stream().map(DtoMapper::toAttendance).toList() : List.of(),
                !full);
    }

    @Transactional
    public EmployeeDetail create(EmployeeRequest request) {
        if (employees.existsByEmailIgnoreCase(request.email().trim())) {
            throw ApiException.conflict("An employee with email " + request.email().trim() + " already exists");
        }
        Employee e = new Employee();
        apply(e, request);
        employees.save(e);
        saveAddress(e, request.address());
        auth.createAccountFor(e);
        return getDetail(e.getId());
    }

    @Transactional
    public EmployeeDetail update(Integer id, EmployeeRequest request) {
        Employee e = findEmployee(id);
        if (employees.existsByEmailIgnoreCaseAndIdNot(request.email().trim(), id)) {
            throw ApiException.conflict("Another employee already uses email " + request.email().trim());
        }
        apply(e, request);
        saveAddress(e, request.address());
        auth.syncUsername(e);
        return getDetail(id);
    }

    /** Deletes the employee together with their address, attendance, projects, leave, payroll and reviews. */
    @Transactional
    public void delete(Integer id) {
        if (id == currentUser.currentEmployeeId()) {
            throw ApiException.badRequest("You can't delete your own employee record");
        }
        Employee e = findEmployee(id);
        addresses.deleteByEmployeeId(id);
        attendance.deleteByEmployeeId(id);
        employeeProjects.deleteByEmployeeId(id);
        leaves.deleteByEmployeeId(id);
        payroll.deleteByEmployeeId(id);
        reviews.deleteByEmployeeId(id);
        employees.delete(e);
    }

    private Employee findEmployee(Integer id) {
        return employees.findWithDetailsById(id).orElseThrow(() -> ApiException.notFound("Employee", id));
    }

    private void apply(Employee e, EmployeeRequest r) {
        Department dept = departments.findById(r.departmentId())
                .orElseThrow(() -> ApiException.badRequest("Department " + r.departmentId() + " does not exist"));
        Designation desig = designations.findById(r.designationId())
                .orElseThrow(() -> ApiException.badRequest("Designation " + r.designationId() + " does not exist"));
        if (r.hireDate().isBefore(r.dateOfBirth())) {
            throw ApiException.badRequest("Hire date can't be before the date of birth");
        }

        e.setFirstName(r.firstName().trim());
        e.setLastName(r.lastName().trim());
        e.setGender(r.gender());
        e.setDateOfBirth(r.dateOfBirth());
        e.setEmail(r.email().trim().toLowerCase());
        e.setPhone(r.phone().trim());
        e.setDepartment(dept);
        e.setDesignation(desig);
        e.setSalary(r.salary());
        e.setHireDate(r.hireDate());
    }

    private void saveAddress(Employee e, AddressDto dto) {
        if (dto == null) {
            return;
        }
        Address a = addresses.findFirstByEmployeeIdOrderByIdAsc(e.getId()).orElseGet(Address::new);
        a.setEmployee(e);
        a.setLine(blankToNull(dto.line()));
        a.setCity(blankToNull(dto.city()));
        a.setState(blankToNull(dto.state()));
        a.setPincode(blankToNull(dto.pincode()));
        a.setCountry(blankToNull(dto.country()));
        addresses.save(a);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
