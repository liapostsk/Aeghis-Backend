package common.exception;

import org.springframework.http.HttpStatus;

/** Recurso no encontrado (404). */
public class NotFoundException extends ApiException {

    public NotFoundException(String detail) {
        super(HttpStatus.NOT_FOUND, "not_found", "Resource not found", detail);
    }
    public NotFoundException(String resource, Object id) {
        this(resource + " with id %s not found".formatted(id));
        withMeta("resource", resource).withMeta("id", id);
    }
}
