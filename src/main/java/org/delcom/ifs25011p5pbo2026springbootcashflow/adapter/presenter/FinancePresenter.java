package org.delcom.ifs25011p5pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Format uang / teks / pesan. Bukan tanggung jawab entity. */
public class FinancePresenter {
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.forLanguageTag("id-ID"));

    /** 1000000 -> "Rp 1,000,000.00" */
    public String formatMoney(long amount) {
        return String.format(Locale.US, "Rp %,.2f", BigDecimal.valueOf(amount));
    }

    /** "[<id>] <keterangan> | Rp 1,000,000.00 | [<Pemasukan/Pengeluaran>]" */
    public String formatLine(Transaction tx) {
        return "[" + tx.getId() + "] " + tx.getDescription()
                + " | " + formatMoney(tx.getAmount())
                + " | [" + tx.getType().getLabel() + "]";
    }

    /** "Saldo: Rp 4,975,000.00" */
    public String formatBalance(long balance) {
        return "Saldo: " + formatMoney(balance);
    }

    public String formatDate(LocalDateTime time) {
        return time == null ? "-" : DATE_TIME.format(time);
    }

    public String addedMessage(Transaction tx) {
        return tx.getType().getLabel() + " berhasil dicatat: " + formatLine(tx);
    }

    public String deletedMessage(Transaction tx) {
        return "Transaksi berhasil dihapus: " + formatLine(tx);
    }

    public String errorMessage(String reason) {
        return "Gagal: " + reason;
    }
}
