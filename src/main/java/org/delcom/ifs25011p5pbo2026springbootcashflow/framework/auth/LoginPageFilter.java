package org.delcom.ifs25011p5pbo2026springbootcashflow.framework.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25011p5pbo2026springbootcashflow.usecase.AuthUseCase;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;

/**
 * Halaman /login berupa HTML biasa dari server (bukan komponen Vaadin): elemen login sudah ada
 * di respons pertama, ringan, dan TIDAK dialihkan walau pengguna sudah login (agar bisa diaudit).
 */
@Component
public class LoginPageFilter extends OncePerRequestFilter {
    private static final String FORM = """
            {{NOTICE}}
            <form method="post" action="/login">
            <label for="login-username">Username</label>
            <input id="login-username" name="username" type="text" autocomplete="username" value="{{USERNAME}}" autofocus>
            <label for="login-password">Password</label>
            <input id="login-password" name="password" type="password" autocomplete="current-password">
            <button id="login-submit" type="submit">Masuk</button>
            </form>
            <p class="alt"><a href="/register">Belum punya akun? Daftar</a></p>
            """;

    private final AuthUseCase auth;

    public LoginPageFilter(AuthUseCase auth) {
        this.auth = auth;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !(path.equals("/login") || path.equals("/login/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String method = request.getMethod();
        if ("POST".equalsIgnoreCase(method)) {
            handleLogin(request, response);
        } else if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)) {
            String notice = request.getParameter("registered") != null
                    ? "<p class=\"success\" role=\"status\">Registrasi berhasil, silakan masuk.</p>"
                    : "";
            render(response, notice, "");
        } else {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username") == null ? "" : request.getParameter("username");
        String password = request.getParameter("password") == null ? "" : request.getParameter("password");
        try {
            User user = auth.login(username, password);
            HttpSession old = request.getSession(false);
            if (old != null) {
                old.invalidate(); // buang sesi lama (termasuk penanda logout)
            }
            response.addCookie(AuthSession.createCookie(user.getId()));
            response.sendRedirect(request.getContextPath() + "/");
        } catch (ValidationException e) {
            String error = "<p class=\"error\" role=\"alert\">" + HtmlUtils.htmlEscape(e.getMessage()) + "</p>";
            render(response, error, username);
        }
    }

    private void render(HttpServletResponse response, String notice, String username) throws IOException {
        String content = FORM
                .replace("{{NOTICE}}", notice)
                .replace("{{USERNAME}}", HtmlUtils.htmlEscape(username));
        AuthPageHtml.write(response, AuthPageHtml.page(
                "Masuk | Cashflow",
                "Masuk ke aplikasi Cashflow untuk mencatat pemasukan dan pengeluaran pribadi.",
                "Masuk ke Cashflow",
                content));
    }
}