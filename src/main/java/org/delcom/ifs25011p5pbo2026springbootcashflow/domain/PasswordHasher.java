package org.delcom.ifs25011p5pbo2026springbootcashflow.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Hash / verifikasi password dengan SHA-256 (hex). */
public final class PasswordHasher {
    private PasswordHasher() {
    }

    public static String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 tidak tersedia", e);
        }
    }

    public static boolean matches(String raw, String hash) {
        if (raw == null || hash == null) {
            return false;
        }
        return MessageDigest.isEqual(
                hash(raw).getBytes(StandardCharsets.UTF_8),
                hash.getBytes(StandardCharsets.UTF_8));
    }
}
