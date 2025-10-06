package com.tfg.aegis.common.exception;

import org.springframework.http.HttpStatus;

/** No autenticado (401). */
public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String detail) {
        super(HttpStatus.UNAUTHORIZED, "unauthorized", "Unauthorized", detail);
    }
}