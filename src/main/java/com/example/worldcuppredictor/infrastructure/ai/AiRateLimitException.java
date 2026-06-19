package com.example.worldcuppredictor.infrastructure.ai;

/**
 * Thrown when the AI provider signals that the request limit has been exceeded (HTTP 429).
 *
 * <p>The {@link com.example.worldcuppredictor.infrastructure.exception.GlobalExceptionHandler}
 * maps this exception to an HTTP 429 response so that callers can implement back-off retry logic.
 */
public class AiRateLimitException extends RuntimeException {
    public AiRateLimitException(String message) {
        super(message);
    }

    public AiRateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
