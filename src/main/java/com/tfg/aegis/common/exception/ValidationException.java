package com.tfg.aegis.common.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

/** Regla de negocio inválida (400). Diferente a Bean Validation del controller. */
public class ValidationException extends ApiException {
    public ValidationException(String detail) {
        super(HttpStatus.BAD_REQUEST, "validation_error", "Validation error", detail);
    }
    /** Incluye errores por campo, p. ej. {"email":"formato inválido"} */
    public ValidationException(String detail, Map<String, String> fieldErrors) {
        this(detail);
        if (fieldErrors != null) getMeta().put("errors", fieldErrors);
    }
}