package common.exception;

import org.springframework.http.HttpStatus;

/** Petición inválida (400). */
public class BadRequestException extends ApiException {
    public BadRequestException(String detail) {
        super(HttpStatus.BAD_REQUEST, "bad_request", "Bad Request", detail);
    }
}