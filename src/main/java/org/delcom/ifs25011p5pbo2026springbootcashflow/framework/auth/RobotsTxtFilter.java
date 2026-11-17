package org.delcom.ifs25011p5pbo2026springbootcashflow.framework.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Melayani /robots.txt sebagai teks polos yang valid, tanpa melewati Vaadin. */
@Component
public class RobotsTxtFilter extends OncePerRequestFilter {
    private static final String BODY = "User-agent: *\nAllow: /\n";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !path.equals("/robots.txt");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Cache-Control", "public, max-age=3600");
        response.getWriter().write(BODY);
    }
}