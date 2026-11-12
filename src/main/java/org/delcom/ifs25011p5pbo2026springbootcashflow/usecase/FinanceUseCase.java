package org.delcom.ifs25011p5pbo2026springbootcashflow.usecase;

import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;
import java.util.Locale;

/** Proses bisnis cashflow. Tidak ada komponen UI / System.out di sini. */
public class FinanceUseCase {
    private final ITransactionRepository repository;

    public FinanceUseCase(ITransactionRepository repository) {
        this.repository = repository;
    }

    public Transaction addIncome(int userId, String description, String amountRaw, String proofPath)
            throws ValidationException {
        return add(userId, TransactionType.INCOME, description, amountRaw, proofPath);
    }

    public Transaction addExpense(int userId, String description, String amountRaw, String proofPath)
            throws ValidationException {
        return add(userId, TransactionType.EXPENSE, description, amountRaw, proofPath);
    }

    public Transaction add(int userId, TransactionType type, String description, String amountRaw, String proofPath)
            throws ValidationException {
        validateUserId(userId);
        String cleanDescription = validateDescription(description);
        long amount = parseAmount(amountRaw);
        return repository.save(userId, cleanDescription, amount, type, blankToNull(proofPath));
    }

    /** Cari (case-insensitive pada keterangan) + urutkan, hanya milik userId. */
    public List<Transaction> list(int userId, String keyword, SortOption sort) {
        String needle = keyword == null ? "" : keyword.strip().toLowerCase(Locale.ROOT);
        SortOption option = sort == null ? SortOption.NEWEST : sort;
        return repository.findByUserId(userId).stream()
                .filter(t -> t.getDescription().toLowerCase(Locale.ROOT).contains(needle))
                .sorted(option.getComparator())
                .toList();
    }

    public Transaction update(int actorUserId, int id, String description, String amountRaw, TransactionType type)
            throws DomainException {
        Transaction tx = getOwned(actorUserId, id);
        String cleanDescription = validateDescription(description);
        long amount = parseAmount(amountRaw);
        if (type == null) {
            throw new ValidationException("Tipe transaksi wajib dipilih");
        }
        tx.setDescription(cleanDescription);
        tx.setAmount(amount);
        tx.setType(type);
        return repository.update(tx);
    }

    public Transaction updateProof(int actorUserId, int id, String proofPath) throws DomainException {
        Transaction tx = getOwned(actorUserId, id);
        tx.setProofPath(blankToNull(proofPath));
        return repository.update(tx);
    }

    /** Menghapus transaksi milik sendiri; mengembalikan transaksi agar pemanggil bisa menghapus file bukti. */
    public Transaction delete(int actorUserId, int id) throws DomainException {
        Transaction tx = getOwned(actorUserId, id);
        repository.deleteById(id);
        return tx;
    }

    public BalanceSummary summarize(int userId) {
        List<Transaction> all = repository.findByUserId(userId);
        long income = 0;
        long expense = 0;
        for (Transaction t : all) {
            if (t.getType() == TransactionType.INCOME) {
                income += t.getAmount();
            } else {
                expense += t.getAmount();
            }
        }
        return new BalanceSummary(all.size(), income, expense);
    }

    public long getBalance(int userId) {
        return summarize(userId).balance();
    }

    // ---------- helper ----------

    private Transaction getOwned(int actorUserId, int id) throws DomainException {
        if (id <= 0) {
            throw new ValidationException("ID transaksi tidak valid");
        }
        Transaction tx = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaksi", id));
        if (tx.getUserId() != actorUserId) {
            throw new ValidationException("Transaksi bukan milik Anda");
        }
        return tx;
    }

    private static void validateUserId(int userId) throws ValidationException {
        if (userId <= 0) {
            throw new ValidationException("ID pengguna tidak valid");
        }
    }

    private static String validateDescription(String description) throws ValidationException {
        if (description == null || description.isBlank()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        return description.strip();
    }

    static long parseAmount(String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("Jumlah tidak boleh kosong");
        }
        long value;
        try {
            value = Long.parseLong(raw.strip());
        } catch (NumberFormatException e) {
            throw new ValidationException("Jumlah harus berupa angka bulat tanpa titik/koma");
        }
        if (value <= 0) {
            throw new ValidationException("Jumlah harus lebih dari 0");
        }
        return value;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
