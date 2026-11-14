package org.delcom.ifs25011p5pbo2026springbootcashflow.framework.auth;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Kerangka HTML halaman publik (login/register): ringan, tanpa JavaScript, aksesibel. */
final class AuthPageHtml {
    private static final String TEMPLATE = """
            <!DOCTYPE html>
            <html lang="id">
            <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <meta name="description" content="{{DESCRIPTION}}">
            <meta name="robots" content="index, follow">
            <link rel="icon" href="data:,">
            <title>{{TITLE}}</title>
            <style>
            *{box-sizing:border-box}
            body{margin:0;min-height:100vh;display:flex;align-items:center;justify-content:center;
                 background:#f3f4f6;font-family:Roboto,"Segoe UI",Arial,sans-serif;color:#1f2937}
            main{width:380px;max-width:92vw;background:#fff;border:1px solid #d1d5db;border-radius:12px;
                 box-shadow:0 4px 16px rgba(0,0,0,.08);padding:32px;margin:16px 0}
            h1{margin:0 0 24px;font-size:1.75rem}
            label{display:block;margin:0 0 6px;font-size:.9rem;color:#374151}
            input{display:block;width:100%;height:44px;margin:0 0 18px;padding:0 12px;border:1px solid #9ca3af;
                  border-radius:6px;background:#f9fafb;font-size:1rem;color:#111827}
            input:focus{outline:2px solid #2563eb;outline-offset:1px;background:#fff}
            button{width:100%;height:44px;border:0;border-radius:6px;background:#1d4ed8;color:#fff;
                   font-size:1rem;font-weight:600;cursor:pointer}
            button:hover{background:#1e40af}
            .error,.success{margin:0 0 16px;padding:10px 12px;border-radius:6px}
            .error{background:#fee2e2;color:#7f1d1d}
            .success{background:#dcfce7;color:#14532d}
            .alt{margin:18px 0 0;text-align:right}
            a{color:#1d4ed8;text-decoration:underline}
            </style>
            </head>
            <body>
            <main>
            <h1>{{HEADING}}</h1>
            {{CONTENT}}
            </main>
            </body>
            </html>
            """;

    private AuthPageHtml() {
    }

    /** CONTENT diganti terakhir supaya isi dari pengguna tidak ikut diproses sebagai placeholder. */
    static String page(String title, String description, String heading, String content) {
        return TEMPLATE
                .replace("{{TITLE}}", title)
                .replace("{{DESCRIPTION}}", description)
                .replace("{{HEADING}}", heading)
                .replace("{{CONTENT}}", content);
    }

    static void write(HttpServletResponse response, String html) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(html);
    }
}
