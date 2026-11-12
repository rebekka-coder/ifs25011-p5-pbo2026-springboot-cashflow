package org.delcom.ifs25011p5pbo2026springbootcashflow.usecase;

/** Ringkasan keuangan satu pengguna. */
public record BalanceSummary(int count, long totalIncome, long totalExpense) {
    public long balance() {
        return totalIncome - totalExpense;
    }
}
