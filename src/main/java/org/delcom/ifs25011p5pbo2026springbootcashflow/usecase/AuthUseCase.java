package org.delcom.ifs25011p5pbo2026springbootcashflow.usecase;

import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.Role;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.repository.IUserRepository;

import java.util.Optional;

public class AuthUseCase {
    static final int MIN_PASSWORD = 6;

    private final IUserRepository users;

    public AuthUseCase(IUserRepository users) {
        this.users = users;
    }

    public User register(String username, String password, String fullName) throws ValidationException {
        String cleanUsername = validateUsername(username);
        if (password == null || password.length() < MIN_PASSWORD) {
            throw new ValidationException("Password minimal " + MIN_PASSWORD + " karakter");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("Nama lengkap tidak boleh kosong");
        }
        if (users.findByUsername(cleanUsername).isPresent()) {
            throw new ValidationException("Username sudah dipakai");
        }
        return users.save(cleanUsername, PasswordHasher.hash(password), fullName.strip(), Role.USER);
    }

    public User login(String username, String password) throws ValidationException {
        Optional<User> found = username == null ? Optional.empty() : users.findByUsername(username.strip());
        if (found.isEmpty() || !PasswordHasher.matches(password, found.get().getPasswordHash())) {
            throw new ValidationException("Username atau password salah");
        }
        return found.get();
    }

    /** Seed admin dari ROOT_USERNAME / ROOT_PASSWORD bila belum ada. */
    public User ensureRootAdmin(String username, String password) throws ValidationException {
        String cleanUsername = validateUsername(username);
        if (password == null || password.isBlank()) {
            throw new ValidationException("Password root tidak boleh kosong");
        }
        Optional<User> existing = users.findByUsername(cleanUsername);
        if (existing.isPresent()) {
            return existing.get();
        }
        return users.save(cleanUsername, PasswordHasher.hash(password), "Administrator", Role.ADMIN);
    }

    private static String validateUsername(String username) throws ValidationException {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username tidak boleh kosong");
        }
        String clean = username.strip();
        if (!clean.matches("[A-Za-z0-9._-]{3,50}")) {
            throw new ValidationException("Username 3-50 karakter (huruf, angka, titik, garis bawah, strip)");
        }
        return clean;
    }
}
