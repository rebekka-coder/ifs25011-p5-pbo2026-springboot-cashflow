package org.delcom.ifs25011p5pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.Role;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.repository.IUserRepository;

import java.util.Optional;

/** Implementasi in-memory IUserRepository (unit test). */
public class UserRepository extends InMemoryRepository<User, Integer> implements IUserRepository {
    private int sequence = 0;

    @Override
    protected Integer idOf(User entity) {
        return entity.getId();
    }

    @Override
    protected String entityName() {
        return "Pengguna";
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        User user = new User(++sequence, username, passwordHash, fullName, role, null);
        store.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return store.values().stream().filter(u -> u.getUsername().equals(username)).findFirst();
    }
}
