package com.employeemanagement.security;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Writes 401 and 403 responses in the same problem-detail shape the rest of the API uses. */
@Component
public class ProblemJsonHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
            throws IOException {
        // Only our own message is echoed back; library messages can contain token details.
        String detail = AccountJwtConverter.SESSION_ENDED.equals(ex.getMessage())
                ? AccountJwtConverter.SESSION_ENDED
                : "Please sign in to continue.";
        response.setHeader("WWW-Authenticate", "Bearer");
        write(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", detail);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException {
        write(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden", "You don't have permission to do that.");
    }

    private static void write(HttpServletResponse response, int status, String title, String detail)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"title\":\"" + title
                + "\",\"detail\":\"" + detail + "\"}");
    }
}
