package com.employeemanagement.security;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.employeemanagement.entity.AppUser;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.Role;
import com.employeemanagement.repository.AppUserRepository;
import com.employeemanagement.repository.EmployeeRepository;

/**
 * Practice-data helper: gives every employee without an account a login (username = email,
 * password = app.security.default-password). Employees 1-3 get the ADMIN, HR and MANAGER roles
 * so every permission level can be tried. Existing accounts are never touched.
 */
@Component
public class UserAccountSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UserAccountSeeder.class);
    private static final Map<Integer, Role> DEMO_ROLES = Map.of(1, Role.ADMIN, 2, Role.HR, 3, Role.MANAGER);

    private final AppUserRepository users;
    private final EmployeeRepository employees;
    private final PasswordEncoder passwordEncoder;
    private final String defaultPassword;

    public UserAccountSeeder(AppUserRepository users, EmployeeRepository employees, PasswordEncoder passwordEncoder,
                             @Value("${app.security.default-password}") String defaultPassword) {
        this.users = users;
        this.employees = employees;
        this.passwordEncoder = passwordEncoder;
        this.defaultPassword = defaultPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Integer> missing = users.findEmployeeIdsWithoutAccount();
        if (missing.isEmpty()) {
            return;
        }
        // One hash shared by all seeded accounts keeps startup fast; each account gets its own on change.
        String hash = passwordEncoder.encode(defaultPassword);
        int created = 0;
        for (Employee e : employees.findAllById(missing)) {
            if (e.getEmail() == null || e.getEmail().isBlank()) {
                continue;
            }
            String username = e.getEmail().trim().toLowerCase();
            if (users.existsByUsername(username)) {
                continue;
            }
            AppUser user = new AppUser();
            user.setEmployee(e);
            user.setUsername(username);
            user.setPasswordHash(hash);
            user.setRole(DEMO_ROLES.getOrDefault(e.getId(), Role.EMPLOYEE));
            users.save(user);
            created++;
        }
        log.info("Created {} login accounts with the default password. Sign in with an employee's email, "
                + "e.g. employee1@company.com (ADMIN), employee2@company.com (HR), employee3@company.com (MANAGER).",
                created);
    }
}
