package org.delcom.ifs25011p5pbo2026springbootcashflow.domain.exception;

/** Checked exception dasar untuk semua kegagalan aturan bisnis. */
public class DomainException extends Exception {
    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
