package org.delcom.ifs25011p5pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.repository.ITransactionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class JdbcTransactionRepository implements ITransactionRepository {
    private static final String COLUMNS = "id, user_id, description, amount, type, proof_path, created_at";

    private static final RowMapper<Transaction> MAPPER = (rs, i) -> new Transaction(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("description"),
            rs.getLong("amount"),
            TransactionType.valueOf(rs.getString("type")),
            rs.getString("proof_path"),
            rs.getTimestamp("created_at").toLocalDateTime());

    private final JdbcTemplate jdbc;

    public JdbcTransactionRepository(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type, String proofPath) {
        LocalDateTime now = LocalDateTime.now();
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO transactions (user_id, description, amount, type, proof_path, created_at) "
                            + "VALUES (?, ?, ?, ?, ?, ?)",
                    new String[]{"id"});
            ps.setInt(1, userId);
            ps.setString(2, description);
            ps.setLong(3, amount);
            ps.setString(4, type.name());
            ps.setString(5, proofPath);
            ps.setTimestamp(6, Timestamp.valueOf(now));
            return ps;
        }, keys);
        return new Transaction(keys.getKey().intValue(), userId, description, amount, type, proofPath, now);
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM transactions WHERE user_id = ? ORDER BY id", MAPPER, userId);
    }

    @Override
    public Optional<Transaction> findById(Integer id) {
        return jdbc.query("SELECT " + COLUMNS + " FROM transactions WHERE id = ?", MAPPER, id)
                .stream().findFirst();
    }

    @Override
    public List<Transaction> findAll() {
        return jdbc.query("SELECT " + COLUMNS + " FROM transactions ORDER BY id", MAPPER);
    }

    @Override
    public List<Transaction> findBy(Predicate<Transaction> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

    @Override
    public Transaction update(Transaction tx) throws EntityNotFoundException {
        int rows = jdbc.update(
                "UPDATE transactions SET description = ?, amount = ?, type = ?, proof_path = ? WHERE id = ?",
                tx.getDescription(), tx.getAmount(), tx.getType().name(), tx.getProofPath(), tx.getId());
        if (rows == 0) {
            throw new EntityNotFoundException("Transaksi", tx.getId());
        }
        return tx;
    }

    @Override
    public void deleteById(Integer id) throws EntityNotFoundException {
        if (jdbc.update("DELETE FROM transactions WHERE id = ?", id) == 0) {
            throw new EntityNotFoundException("Transaksi", id);
        }
    }
}
