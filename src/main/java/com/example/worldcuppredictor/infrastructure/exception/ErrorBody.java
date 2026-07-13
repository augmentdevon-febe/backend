package com.example.worldcuppredictor.infrastructure.exception;

import java.util.ArrayList;
import java.util.List;

public class ErrorBody {
    private ErrorCode code;
    private String message;
    private List<String> details = new ArrayList<>();

    public ErrorBody() {
    }

    public ErrorBody(ErrorCode code, String message, List<String> details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public ErrorCode getCode() {
        return code;
    }

    public void setCode(ErrorCode code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }
}
