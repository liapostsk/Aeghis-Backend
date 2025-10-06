package com.tfg.aegis.common.exception;

import org.springframework.http.HttpStatus;

/** Servicio no disponible temporalmente (503). */
public class ServiceUnavailableException extends ApiException {
    public ServiceUnavailableException(String detail) {
        super(HttpStatus.SERVICE_UNAVAILABLE, "service_unavailable", "Service Unavailable", detail);
    }
}