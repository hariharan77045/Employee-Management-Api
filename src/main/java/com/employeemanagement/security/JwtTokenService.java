package com.employeemanagement.security;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.employeemanagement.entity.AppUser;

/** Issues the HS256-signed access tokens returned by /api/auth/login. */
@Service
public class JwtTokenService {

    public static final String ISSUER = "employee-hub";
    public static final String CLAIM_EMPLOYEE_ID = "eid";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_TOKEN_VERSION = "ver";

    private final JwtEncoder encoder;
    private final Duration lifetime;

    public JwtTokenService(JwtEncoder encoder, @Value("${app.jwt.expiry-minutes}") long expiryMinutes) {
        this.encoder = encoder;
        this.lifetime = Duration.ofMinutes(expiryMinutes);
    }

    public IssuedToken issue(AppUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(lifetime);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("username", user.getUsername())
                .claim(CLAIM_EMPLOYEE_ID, user.getEmployee().getId())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_TOKEN_VERSION, user.getTokenVersion())
                .build();
        String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return new IssuedToken(token, expiresAt);
    }

    public record IssuedToken(String value, Instant expiresAt) {
    }
}
