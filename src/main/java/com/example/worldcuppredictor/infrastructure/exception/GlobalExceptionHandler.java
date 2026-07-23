package com.example.worldcuppredictor.infrastructure.exception;

import com.example.worldcuppredictor.infrastructure.ai.AiRateLimitException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Centralised exception handler that converts application exceptions into structured
 * {@link ApiErrorResponse} JSON payloads with appropriate HTTP status codes.
 *
 * <ul>
 *   <li>{@link ResponseStatusException} → mapped response status contract</li>
 *   <li>{@link AiRateLimitException} → 429 (OpenAI rate limit exceeded)</li>
 *   <li>{@link ServiceUnavailableException} → 503 (temporary outage)</li>
 *   <li>{@link Exception} → 500 (any other unhandled exception)</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        ApiErrorResponse r = ApiErrorFactory.fromStatus(status, req.getRequestURI());
        return ResponseEntity.status(status).body(r);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(IllegalArgumentException ex, HttpServletRequest req) {
        ApiErrorResponse r = ApiErrorFactory.fromStatus(HttpStatus.BAD_REQUEST, req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(r);
    }

    /** Handles OpenAI rate-limit errors so callers receive 429 instead of 500. */
    @ExceptionHandler(AiRateLimitException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimit(AiRateLimitException ex, HttpServletRequest req) {
        ApiErrorResponse r = ApiErrorFactory.build(
                ErrorCode.RATE_LIMITED,
                "Too many requests. Try again later.",
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(r);
    }

    /** Handles access denied scenarios and maps to strict 403 schema. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(AccessDeniedException ex, HttpServletRequest req) {
        ApiErrorResponse r = ApiErrorFactory.build(
                ErrorCode.FORBIDDEN,
                "You do not have access to this resource.",
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(r);
    }

    /** Handles transient backend dependency outages. */
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleServiceUnavailable(ServiceUnavailableException ex, HttpServletRequest req) {
        ApiErrorResponse r = ApiErrorFactory.build(
                ErrorCode.SERVICE_UNAVAILABLE,
                "Service temporarily unavailable.",
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(r);
    }

    /** Catch-all handler for unexpected exceptions. Returns a generic 500 response. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAll(Exception ex, HttpServletRequest req) {
        ApiErrorResponse r = ApiErrorFactory.build(
                ErrorCode.INTERNAL_ERROR,
                "Unexpected server error.",
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(r);
    }
}
