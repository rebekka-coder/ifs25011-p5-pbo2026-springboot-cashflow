package org.delcom.ifs25011p5pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.Role;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class JdbcUserRepository implements IUserRepository {
    private static final String COLUMNS = "id, username, password_hash, full_name, role, photo_path";

    private static final RowMapper<User> MAPPER = (rs, i) -> new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("full_name"),
            Role.valueOf(rs.getString("role")),
            rs.getString("photo_path"));

    private final JdbcTemplate jdbc;

    public JdbcUserRepository(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO users (username, password_hash, full_name, role) VALUES (?, ?, ?, ?)",
                    new String[]{"id"});
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, fullName);
            ps.setString(4, role.name());
            return ps;
        }, keys);
        int id = keys.getKey().intValue();
        return new User(id, username, passwordHash, fullName, role, null);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jdbc.query("SELECT " + COLUMNS + " FROM users WHERE username = ?", MAPPER, username)
                .stream().findFirst();
    }

    @Override
    public Optional<User> findById(Integer id) {
        return jdbc.query("SELECT " + COLUMNS + " FROM users WHERE id = ?", MAPPER, id)
                .stream().findFirst();
    }

    @Override
    public List<User> findAll() {
        return jdbc.query("SELECT " + COLUMNS + " FROM users ORDER BY id", MAPPER);
    }

    @Override
    public List<User> findBy(Predicate<User> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

    @Override
    public User update(User user) throws EntityNotFoundException {
        int rows = jdbc.update(
                "UPDATE users SET password_hash = ?, full_name = ?, photo_path = ? WHERE id = ?",
                user.getPasswordHash(), user.getFullName(), user.getPhotoPath(), user.getId());
        if (rows == 0) {
            throw new EntityNotFoundException("Pengguna", user.getId());
        }
        return user;
    }

    @Override
    public void deleteById(Integer id) throws EntityNotFoundException {
        if (jdbc.update("DELETE FROM users WHERE id = ?", id) == 0) {
            throw new EntityNotFoundException("Pengguna", id);
        }
    }
}
