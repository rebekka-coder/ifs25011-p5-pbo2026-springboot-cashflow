package org.delcom.ifs25011p5pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.repository.ITransactionRepository;

import java.time.LocalDateTime;
import java.util.List;

/** Implementasi in-memory ITransactionRepository (unit test). */
public class TransactionRepository extends InMemoryRepository<Transaction, Integer> implements ITransactionRepository {
    private int sequence = 0;

    @Override
    protected Integer idOf(Transaction entity) {
        return entity.getId();
    }

    @Override
    protected String entityName() {
        return "Transaksi";
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type, String proofPath) {
        Transaction tx = new Transaction(++sequence, userId, description, amount, type, proofPath, LocalDateTime.now());
        store.put(tx.getId(), tx);
        return tx;
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return findBy(t -> t.getUserId() == userId);
    }
}
