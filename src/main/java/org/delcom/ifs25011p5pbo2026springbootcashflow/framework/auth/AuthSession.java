package org.delcom.ifs25011p5pbo2026springbootcashflow.framework.auth;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;
import jakarta.servlet.http.Cookie;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.repository.IUserRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

/**
 * Sesi login: user id disimpan di VaadinSession + HTTP session + cookie bertanda tangan (HMAC).
 * LOGGED_OUT_KEY mencegah sesi dipulihkan dari cookie setelah logout.
 */
public final class AuthSession {
    public static final String COOKIE_NAME = "cashflow_uid";
    private static final String USER_ID_KEY = "cashflow.auth.userId";
    private static final String LOGGED_OUT_KEY = "cashflow.auth.loggedOut";
    private static final int COOKIE_MAX_AGE = 8 * 60 * 60;

    private static volatile byte[] secret = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);

    private AuthSession() {
    }

    /** Dipanggil saat start; secret kosong = tetap memakai secret acak (cookie tidak valid setelah restart). */
    public static void configure(String sessionSecret) {
        if (sessionSecret != null && !sessionSecret.isBlank()) {
            secret = sessionSecret.getBytes(StandardCharsets.UTF_8);
        }
    }

    public static void login(User user) {
        VaadinSession vaadin = VaadinSession.getCurrent();
        vaadin.setAttribute(LOGGED_OUT_KEY, null);
        vaadin.setAttribute(USER_ID_KEY, user.getId());
        WrappedSession http = vaadin.getSession();
        if (http != null) {
            http.setAttribute(USER_ID_KEY, user.getId());
        }
        writeCookie(sign(user.getId()), COOKIE_MAX_AGE);
    }

    public static Optional<User> currentUser(IUserRepository users) {
        VaadinSession vaadin = VaadinSession.getCurrent();
        if (vaadin == null) {
            return Optional.empty();
        }
        Integer id = asInt(vaadin.getAttribute(USER_ID_KEY));
        if (id == null && vaadin.getSession() != null) {
            id = asInt(vaadin.getSession().getAttribute(USER_ID_KEY));
        }
        if (id == null && !Boolean.TRUE.equals(vaadin.getAttribute(LOGGED_OUT_KEY))) {
            id = idFromCookie();
        }
        if (id == null) {
            return Optional.empty();
        }
        Optional<User> user = users.findById(id);
        if (user.isPresent()) {
            vaadin.setAttribute(USER_ID_KEY, id);
        } else {
            clear(vaadin);
        }
        return user;
    }

    public static void logout() {
        VaadinSession vaadin = VaadinSession.getCurrent();
        if (vaadin != null) {
            clear(vaadin);
            vaadin.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        }
        writeCookie("", 0);
    }

    // ---------- internal ----------

    private static void clear(VaadinSession vaadin) {
        vaadin.setAttribute(USER_ID_KEY, null);
        if (vaadin.getSession() != null) {
            vaadin.getSession().removeAttribute(USER_ID_KEY);
        }
    }

    private static Integer asInt(Object value) {
        return value instanceof Integer i ? i : null;
    }

    private static void writeCookie(String value, int maxAge) {
        VaadinResponse response = VaadinService.getCurrentResponse();
        if (response == null) {
            return;
        }
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private static Integer idFromCookie() {
        VaadinRequest request = VaadinService.getCurrentRequest();
        if (request == null || request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return verify(cookie.getValue());
            }
        }
        return null;
    }

    private static String sign(int id) {
        return id + "." + hmac(String.valueOf(id));
    }

    private static Integer verify(String token) {
        if (token == null) {
            return null;
        }
        int dot = token.indexOf('.');
        if (dot <= 0) {
            return null;
        }
        String idPart = token.substring(0, dot);
        String sig = token.substring(dot + 1);
        boolean valid = MessageDigest.isEqual(
                hmac(idPart).getBytes(StandardCharsets.UTF_8), sig.getBytes(StandardCharsets.UTF_8));
        if (!valid) {
            return null;
        }
        try {
            return Integer.valueOf(idPart);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC tidak tersedia", e);
        }
    }
}
