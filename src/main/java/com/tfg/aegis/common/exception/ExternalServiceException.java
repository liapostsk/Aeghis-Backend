package com.tfg.aegis.common.exception;

import org.springframework.http.HttpStatus;

/** Error al llamar a un servicio externo (502 por defecto). */
public class ExternalServiceException extends ApiException {
    public ExternalServiceException(String service, String detail) {
        super(HttpStatus.BAD_GATEWAY, "external_service_error", "External service error",
                "[%s] %s".formatted(service, detail));
        withMeta("service", service);
    }
    /** Permite especificar otro status (503/504) según el caso. */
    public ExternalServiceException(HttpStatus status, String service, String detail) {
        super(status, "external_service_error", "External service error",
                "[%s] %s".formatted(service, detail));
        withMeta("service", service);
    }
}
