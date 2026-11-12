package org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity;

import java.time.LocalDateTime;
import java.util.Comparator;

/** Opsi pengurutan transaksi; tiap opsi membawa Comparator. */
public enum SortOption {
    NEWEST("Terbaru",
            Comparator.comparing(Transaction::getCreatedAt, Comparator.<LocalDateTime>reverseOrder())
                    .thenComparing(Transaction::getId, Comparator.<Integer>reverseOrder())),
    OLDEST("Terlama",
            Comparator.comparing(Transaction::getCreatedAt)
                    .thenComparing(Transaction::getId)),
    AMOUNT_ASC("Jumlah: kecil ke besar",
            Comparator.comparingLong(Transaction::getAmount)
                    .thenComparing(Transaction::getId)),
    AMOUNT_DESC("Jumlah: besar ke kecil",
            Comparator.comparingLong(Transaction::getAmount).reversed()
                    .thenComparing(Transaction::getId)),
    INCOME_FIRST("Pemasukan dulu",
            Comparator.comparingInt((Transaction t) -> t.getType() == TransactionType.INCOME ? 0 : 1)
                    .thenComparing(Transaction::getId)),
    EXPENSE_FIRST("Pengeluaran dulu",
            Comparator.comparingInt((Transaction t) -> t.getType() == TransactionType.EXPENSE ? 0 : 1)
                    .thenComparing(Transaction::getId));

    private final String label;
    private final Comparator<Transaction> comparator;

    SortOption(String label, Comparator<Transaction> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public String getLabel() {
        return label;
    }

    public Comparator<Transaction> getComparator() {
        return comparator;
    }
}
