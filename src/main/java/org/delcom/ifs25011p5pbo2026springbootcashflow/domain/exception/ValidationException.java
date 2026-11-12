package org.delcom.ifs25011p5pbo2026springbootcashflow.domain.exception;

/** Validasi input / aturan bisnis gagal. */
public class ValidationException extends DomainException {
    public ValidationException(String message) {
        super(message);
    }
}
