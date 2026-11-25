package com.employeemanagement.security;

import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;

import com.employeemanagement.entity.AppUser;
import com.employeemanagement.repository.AppUserRepository;

/**
 * Turns a valid token into an authenticated principal. The account is re-read on every request,
 * so disabling an account, changing its role or resetting its password takes effect immediately
 * instead of waiting for old tokens to expire.
 */
@Component
public class AccountJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    public static final String SESSION_ENDED = "Your session has ended. Please sign in again.";

    private final AppUserRepository users;

    public AccountJwtConverter(AppUserRepository users) {
        this.users = users;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        AppUser user;
        try {
            user = users.findById(Integer.valueOf(jwt.getSubject())).orElse(null);
        } catch (NumberFormatException ex) {
            user = null;
        }
        Number version = jwt.getClaim(JwtTokenService.CLAIM_TOKEN_VERSION);
        Number employeeId = jwt.getClaim(JwtTokenService.CLAIM_EMPLOYEE_ID);
        if (user == null || !user.isEnabled() || version == null || employeeId == null
                || version.intValue() != user.getTokenVersion()) {
            throw new InvalidBearerTokenException(SESSION_ENDED);
        }

        var principal = new AccountPrincipal(user.getId(), employeeId.intValue(), user.getUsername(), user.getRole());
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return UsernamePasswordAuthenticationToken.authenticated(principal, null, authorities);
    }
}
