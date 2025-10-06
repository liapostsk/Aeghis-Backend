package com.tfg.aegis.common.exception;

import org.springframework.http.HttpStatus;

/** Conflicto de concurrencia (409), p. ej. versión/ETag. */
public class ConcurrencyException extends ApiException {
    public ConcurrencyException(String detail) {
        super(HttpStatus.CONFLICT, "concurrency_conflict", "Concurrency conflict", detail);
    }
}
