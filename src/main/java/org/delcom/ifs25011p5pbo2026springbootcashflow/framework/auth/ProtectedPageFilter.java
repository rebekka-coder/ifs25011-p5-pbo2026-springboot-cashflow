package org.delcom.ifs25011p5pbo2026springbootcashflow.framework.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Halaman terproteksi: bila belum login (cookie tidak valid), langsung diarahkan
 * ke /login dengan HTTP 302 dari server, tanpa menunggu JavaScript Vaadin.
 */
@Component
public class ProtectedPageFilter extends OncePerRequestFilter {
    private static final Set<String> PROTECTED = Set.of("/", "/profile", "/users", "/user-transactions");

    private final IUserRepository users;

    public ProtectedPageFilter(IUserRepository users) {
        this.users = users;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (request.getParameter("v-r") != null) {
            return true; // request internal Vaadin
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return !PROTECTED.contains(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Integer userId = AuthSession.userIdFromRequest(request);
        if (userId != null && users.findById(userId).isPresent()) {
            chain.doFilter(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }
}