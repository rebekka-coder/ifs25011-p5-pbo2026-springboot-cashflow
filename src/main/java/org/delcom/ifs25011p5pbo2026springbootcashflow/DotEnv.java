package org.delcom.ifs25011p5pbo2026springbootcashflow;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Memuat file .env ke System properties sebelum Spring Boot start (env OS tetap diprioritaskan). */
public final class DotEnv {
    private DotEnv() {
    }

    public static void load() {
        load(Path.of(".env"));
    }

    public static void load(Path file) {
        if (!Files.isRegularFile(file)) {
            return;
        }
        try {
            for (String raw : Files.readAllLines(file)) {
                String line = raw.strip();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = line.substring(0, eq).strip();
                String value = unquote(line.substring(eq + 1).strip());
                if (System.getenv(key) == null && System.getProperty(key) == null) {
                    System.setProperty(key, value);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Gagal membaca " + file, e);
        }
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
