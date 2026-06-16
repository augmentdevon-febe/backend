package com.example.worldcuppredictor.api.dto.response;

public class ExternalAiRawResponse {
    private String rawText;
    private String model;
    private String provider;

    public ExternalAiRawResponse() {
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
