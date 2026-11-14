package org.delcom.ifs25011p5pbo2026springbootcashflow.framework.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25011p5pbo2026springbootcashflow.usecase.AuthUseCase;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;

/** Halaman /register berupa HTML biasa dari server (ringan, tanpa pengalihan walau sudah login). */
@Component
public class RegisterPageFilter extends OncePerRequestFilter {
    private static final String FORM = """
            {{NOTICE}}
            <form method="post" action="/register">
            <label for="register-fullname">Nama lengkap</label>
            <input id="register-fullname" name="fullName" type="text" autocomplete="name" value="{{FULLNAME}}">
            <label for="register-username">Username</label>
            <input id="register-username" name="username" type="text" autocomplete="username" value="{{USERNAME}}">
            <label for="register-password">Password</label>
            <input id="register-password" name="password" type="password" autocomplete="new-password">
            <label for="register-confirm">Ulangi password</label>
            <input id="register-confirm" name="confirm" type="password" autocomplete="new-password">
            <button id="register-submit" type="submit">Daftar</button>
            </form>
            <p class="alt"><a href="/login">Sudah punya akun? Masuk</a></p>
            """;

    private final AuthUseCase auth;

    public RegisterPageFilter(AuthUseCase auth) {
        this.auth = auth;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !(path.equals("/register") || path.equals("/register/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String method = request.getMethod();
        if ("POST".equalsIgnoreCase(method)) {
            handleRegister(request, response);
        } else if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)) {
            render(response, "", "", "");
        } else {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        String fullName = param(request, "fullName");
        String username = param(request, "username");
        String password = param(request, "password");
        String confirm = param(request, "confirm");
        try {
            if (!password.equals(confirm)) {
                throw new ValidationException("Konfirmasi password tidak sama");
            }
            auth.register(username, password, fullName);
            response.sendRedirect(request.getContextPath() + "/login?registered=1");
        } catch (ValidationException e) {
            String error = "<p class=\"error\" role=\"alert\">" + HtmlUtils.htmlEscape(e.getMessage()) + "</p>";
            render(response, error, fullName, username);
        }
    }

    private static String param(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value;
    }

    private void render(HttpServletResponse response, String notice, String fullName, String username)
            throws IOException {
        String content = FORM
                .replace("{{NOTICE}}", notice)
                .replace("{{FULLNAME}}", HtmlUtils.htmlEscape(fullName))
                .replace("{{USERNAME}}", HtmlUtils.htmlEscape(username));
        AuthPageHtml.write(response, AuthPageHtml.page(
                "Daftar | Cashflow",
                "Buat akun Cashflow untuk mulai mencatat pemasukan dan pengeluaran pribadi.",
                "Daftar Akun Cashflow",
                content));
    }
}