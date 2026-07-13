package com.example.worldcuppredictor.infrastructure.exception;

import java.time.OffsetDateTime;

public class ApiErrorResponse {
    private ErrorBody error;
    private OffsetDateTime timestamp;
    private String path;

    public ApiErrorResponse() {
    }

    public ApiErrorResponse(ErrorBody error, OffsetDateTime timestamp, String path) {
        this.error = error;
        this.timestamp = timestamp;
        this.path = path;
    }

    public ErrorBody getError() {
        return error;
    }

    public void setError(ErrorBody error) {
        this.error = error;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
