package com.employeemanagement.security;

import com.employeemanagement.entity.Role;

/** Who is calling the API, resolved from the bearer token and the app_user row on every request. */
public record AccountPrincipal(Integer userId, Integer employeeId, String username, Role role) {
}
