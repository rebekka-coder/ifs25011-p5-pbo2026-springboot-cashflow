package org.delcom.ifs25011p5pbo2026springbootcashflow.adapter.storage;

import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.exception.ValidationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Menyimpan upload (foto profil + bukti transaksi) ke UPLOAD_DIR dengan nama UUID + ekstensi. */
public class FileStorage {
    public static final long MAX_BYTES = 5L * 1024 * 1024;
    public static final String URL_PREFIX = "uploads/";
    private static final Set<String> ALLOWED = Set.of("png", "jpg", "jpeg", "gif", "webp");

    private final Path root;

    public FileStorage(String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("Tidak bisa membuat folder upload: " + root, e);
        }
    }

    public Path getRoot() {
        return root;
    }

    /** @return path relatif, mis. "uploads/3f2a...png" (diakses lewat /uploads/**). */
    public String store(String originalName, byte[] data) throws ValidationException {
        if (data == null || data.length == 0) {
            throw new ValidationException("File kosong");
        }
        if (data.length > MAX_BYTES) {
            throw new ValidationException("Ukuran file maksimal 5 MB");
        }
        String ext = extensionOf(originalName);
        if (!ALLOWED.contains(ext)) {
            throw new ValidationException("Format gambar harus png, jpg, jpeg, gif, atau webp");
        }
        String filename = UUID.randomUUID() + "." + ext;
        try {
            Files.write(root.resolve(filename), data);
        } catch (IOException e) {
            throw new ValidationException("Gagal menyimpan file: " + e.getMessage());
        }
        return URL_PREFIX + filename;
    }

    /** Hapus file; aman bila path kosong / file tidak ada. Hanya nama file yang dipakai (anti path traversal). */
    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        Path name = Path.of(relativePath).getFileName();
        try {
            Files.deleteIfExists(root.resolve(name));
        } catch (IOException ignored) {
            // file bukti yang tidak bisa dihapus tidak boleh menggagalkan proses bisnis
        }
    }

    private static String extensionOf(String name) {
        if (name == null) {
            return "";
        }
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
