package common.exception;

import org.springframework.http.HttpStatus;

/** Conflicto con el estado actual (409). */
public class ConflictException extends ApiException {
    public ConflictException(String detail) {
        super(HttpStatus.CONFLICT, "conflict", "Conflict", detail);
    }
}