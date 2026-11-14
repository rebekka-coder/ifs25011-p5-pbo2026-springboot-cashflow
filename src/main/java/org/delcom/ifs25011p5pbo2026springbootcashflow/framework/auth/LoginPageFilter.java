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
 * Halaman /login berupa HTML biasa dari server (bukan komponen Vaadin), sehingga
 * #login-username, #login-password, dan #login-submit sudah ada di respons pertama,
 * tanpa menunggu JavaScript. Penting untuk pengecekan otomatis (Lighthouse / Puppeteer).
 */
@Component
public class LoginPageFilter extends OncePerRequestFilter {
    private static final String PAGE = """
            <!DOCTYPE html>
            <html lang="id">
            <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <meta name="description" content="Masuk ke aplikasi Cashflow untuk mencatat pemasukan dan pengeluaran.">
            <title>Masuk | Cashflow</title>
            <style>
            *{box-sizing:border-box}
            body{margin:0;min-height:100vh;display:flex;align-items:center;justify-content:center;
                 background:#f3f4f6;font-family:Roboto,"Segoe UI",Arial,sans-serif;color:#1f2937}
            main{width:380px;max-width:92vw;background:#fff;border:1px solid #e5e7eb;border-radius:12px;
                 box-shadow:0 4px 16px rgba(0,0,0,.08);padding:32px}
            h1{margin:0 0 24px;font-size:1.75rem}
            label{display:block;margin:0 0 6px;font-size:.9rem;color:#4b5563}
            input{display:block;width:100%;height:44px;margin:0 0 18px;padding:0 12px;border:1px solid transparent;
                  border-radius:6px;background:#e9edf2;font-size:1rem}
            input:focus{outline:2px solid #2563EB;background:#fff}
            button{width:100%;height:44px;border:0;border-radius:6px;background:#2563EB;color:#fff;
                   font-size:1rem;font-weight:600;cursor:pointer}
            button:hover{background:#1d4ed8}
            .error{margin:0 0 16px;padding:10px 12px;border-radius:6px;background:#fee2e2;color:#991b1b}
            .alt{margin:18px 0 0;text-align:right}
            a{color:#1d4ed8}
            </style>
            </head>
            <body>
            <main>
            <h1>Masuk ke Cashflow</h1>
            {{ERROR}}
            <form method="post" action="/login">
            <label for="login-username">Username</label>
            <input id="login-username" name="username" type="text" autocomplete="username" value="{{USERNAME}}" autofocus>
            <label for="login-password">Password</label>
            <input id="login-password" name="password" type="password" autocomplete="current-password">
            <button id="login-submit" type="submit">Masuk</button>
            </form>
            <p class="alt"><a href="/register">Belum punya akun? Daftar</a></p>
            </main>
            </body>
            </html>
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
            render(response, "", "");
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
            render(response, e.getMessage(), username);
        }
    }

    private void render(HttpServletResponse response, String error, String username) throws IOException {
        String errorHtml = error.isEmpty()
                ? ""
                : "<p class=\"error\" role=\"alert\">" + HtmlUtils.htmlEscape(error) + "</p>";
        String html = PAGE
                .replace("{{ERROR}}", errorHtml)
                .replace("{{USERNAME}}", HtmlUtils.htmlEscape(username));
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(html);
    }
}