package com.tfg.aegis.common.http;

import com.tfg.aegis.common.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

// Traduce las excepciones lanzadas en los controladores a respuestas HTTP con Problem Details
@RestControllerAdvice
public class ApiExceptionHandler {

    // Excepciones de negocio propias (extienden ApiException)
    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage()); // status + detalle
        pd.setTitle(ex.getTitle()); // resumen
        pd.setType(URI.create("/problems/" + ex.getCode())); // url del tipo de error
        pd.setProperty("code", ex.getCode()); // código de error específico
        pd.setProperty("path", req.getRequestURI());
        ex.getMeta().forEach(pd::setProperty);
        return pd;
    }

    // Validación de @RequestBody (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        FieldError::getDefaultMessage,
                        (a, b) -> a));
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setTitle("Validation error");
        pd.setType(URI.create("/problems/validation_error"));
        pd.setProperty("code", "validation_error");
        pd.setProperty("errors", errors);
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    // Validación de @RequestParam / @PathVariable
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (a, b) -> a));
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setTitle("Validation error");
        pd.setType(URI.create("/problems/validation_error"));
        pd.setProperty("code", "validation_error");
        pd.setProperty("errors", errors);
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    // Colisiones de constraints de BD (únicos, FKs, etc.)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Constraint violation");
        pd.setTitle("Conflict");
        pd.setType(URI.create("/problems/conflict"));
        pd.setProperty("code", "conflict");
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    // Seguridad (autorizado pero sin permiso)
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Access denied");
        pd.setTitle("Forbidden");
        pd.setType(URI.create("/problems/forbidden"));
        pd.setProperty("code", "forbidden");
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    // Fallback 500 para cualquier excepción no controlada
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
        pd.setTitle("Internal Server Error");
        pd.setType(URI.create("/problems/internal_error"));
        pd.setProperty("code", "internal_error");
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }
}
