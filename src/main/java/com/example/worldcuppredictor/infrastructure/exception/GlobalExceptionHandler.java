package com.example.worldcuppredictor.infrastructure.exception;

import com.example.worldcuppredictor.infrastructure.ai.AiRateLimitException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

/**
 * Centralised exception handler that converts application exceptions into structured
 * {@link ApiErrorResponse} JSON payloads with appropriate HTTP status codes.
 *
 * <ul>
 *   <li>{@link MethodArgumentNotValidException} → 400 (bean validation failures on request bodies)</li>
 *   <li>{@link IllegalArgumentException} → 400 (domain validation: unknown team, bad date, etc.)</li>
 *   <li>{@link AiRateLimitException} → 429 (OpenAI rate limit exceeded)</li>
 *   <li>{@link Exception} → 500 (any other unhandled exception)</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /** Handles Jakarta Bean Validation failures on {@code @RequestBody} parameters. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        ApiErrorResponse r = new ApiErrorResponse(OffsetDateTime.now(), 400, "Bad Request", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(r);
    }

    /** Handles domain validation errors (unknown team, same teams, bad ISO-8601 date, etc.). */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegal(IllegalArgumentException ex, HttpServletRequest req) {
        ApiErrorResponse r = new ApiErrorResponse(OffsetDateTime.now(), 400, "Bad Request", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(r);
    }

    /** Handles OpenAI rate-limit errors so callers receive 429 instead of 500. */
    @ExceptionHandler(AiRateLimitException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimit(AiRateLimitException ex, HttpServletRequest req) {
        ApiErrorResponse r = new ApiErrorResponse(OffsetDateTime.now(), 429, "Too Many Requests", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(r);
    }

    /** Catch-all handler for unexpected exceptions. Returns a generic 500 response. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAll(Exception ex, HttpServletRequest req) {
        ApiErrorResponse r = new ApiErrorResponse(OffsetDateTime.now(), 500, "Internal Server Error", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(r);
    }
}
