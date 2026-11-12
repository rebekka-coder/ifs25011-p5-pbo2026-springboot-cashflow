package org.delcom.ifs25011p5pbo2026springbootcashflow.usecase;

import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.repository.IUserRepository;

import java.util.List;

public class UserUseCase {
    private final IUserRepository users;

    public UserUseCase(IUserRepository users) {
        this.users = users;
    }

    public List<User> list() {
        return users.findAll();
    }

    public User get(int id) throws EntityNotFoundException {
        return users.findById(id).orElseThrow(() -> new EntityNotFoundException("Pengguna", id));
    }

    /** newPassword boleh kosong/null (artinya password tidak diubah). */
    public User updateProfile(int id, String fullName, String newPassword) throws DomainException {
        User user = get(id);
        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("Nama lengkap tidak boleh kosong");
        }
        if (newPassword != null && !newPassword.isEmpty()) {
            if (newPassword.length() < AuthUseCase.MIN_PASSWORD) {
                throw new ValidationException("Password minimal " + AuthUseCase.MIN_PASSWORD + " karakter");
            }
            user.setPasswordHash(PasswordHasher.hash(newPassword));
        }
        user.setFullName(fullName.strip());
        return users.update(user);
    }

    public User updatePhoto(int id, String photoPath) throws DomainException {
        User user = get(id);
        user.setPhotoPath(photoPath);
        return users.update(user);
    }
}
