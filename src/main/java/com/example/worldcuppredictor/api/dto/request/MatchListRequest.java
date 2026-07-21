package com.example.worldcuppredictor.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MatchListRequest {
    @NotBlank(message = "identifier is required")
    @Size(max = 100, message = "identifier must be at most 100 characters")
    private String identifier;

    public MatchListRequest() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
