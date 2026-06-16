package com.example.worldcuppredictor.infrastructure.ai;

import com.example.worldcuppredictor.api.dto.response.ExternalAiRawResponse;

import java.util.Map;

public interface AiPredictionClient {
    ExternalAiRawResponse predict(String prompt, Map<String, Object> inputs) throws Exception;
}
