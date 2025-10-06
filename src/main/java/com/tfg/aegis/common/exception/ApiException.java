package com.tfg.aegis.common.exception;

import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/** Raíz de las excepciones de negocio de la API. */
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String code;
    private final String title;
    private final Map<String, Object> meta = new HashMap<>();

    public ApiException(HttpStatus status, String code, String title, String detail) {
        super(detail);
        this.status = status;
        this.code = code;
        this.title = title;
    }

    public ApiException(HttpStatus status, String code, String title, String detail, Throwable cause) {
        super(detail, cause);
        this.status = status;
        this.code = code;
        this.title = title;
    }

    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public Map<String, Object> getMeta() { return meta; }

    /** Añade metadatos (se incluirán en la respuesta de error). */
    public ApiException withMeta(String key, Object value) {
        this.meta.put(key, value);
        return this;
    }
}
