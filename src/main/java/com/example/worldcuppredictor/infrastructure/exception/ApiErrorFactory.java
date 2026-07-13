package com.example.worldcuppredictor.infrastructure.exception;

import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.List;

public final class ApiErrorFactory {
    private ApiErrorFactory() {
    }

    public static ApiErrorResponse build(ErrorCode code, String message, List<String> details, String path) {
        return new ApiErrorResponse(
                new ErrorBody(code, message, details),
                OffsetDateTime.now(),
                path
        );
    }

    public static ApiErrorResponse build(ErrorCode code, String message, String path) {
        return build(code, message, List.of(), path);
    }

    public static ApiErrorResponse fromStatus(HttpStatus status, String path) {
        return switch (status) {
            case UNAUTHORIZED -> build(ErrorCode.UNAUTHENTICATED, "Authentication required.", path);
            case FORBIDDEN -> build(ErrorCode.FORBIDDEN, "You do not have access to this resource.", path);
            case TOO_MANY_REQUESTS -> build(ErrorCode.RATE_LIMITED, "Too many requests. Try again later.", path);
            case SERVICE_UNAVAILABLE -> build(ErrorCode.SERVICE_UNAVAILABLE, "Service temporarily unavailable.", path);
            default -> build(ErrorCode.INTERNAL_ERROR, "Unexpected server error.", path);
        };
    }
}
