package com.example.worldcuppredictor.infrastructure.ai;

import com.example.worldcuppredictor.api.dto.response.ExternalAiRawResponse;

import java.util.Map;

/**
 * Port (strategy interface) for AI-backed match prediction.
 *
 * <p>Implementations are responsible for submitting a prompt to an external AI provider
 * and returning the raw response. The default implementation is {@link OpenAiPredictionClient}.
 * Alternative implementations (e.g. for local models or other providers) can be registered
 * as Spring beans; the {@code @Primary} annotation on the default implementation ensures it
 * is selected unless another bean overrides it.
 */
public interface AiPredictionClient {
    /**
     * Sends {@code prompt} to the configured AI provider and returns the unmodified response.
     *
     * @param prompt plain-text prompt to submit
     * @param inputs optional additional key-value pairs merged into the provider request body
     * @return raw response wrapper (provider name, model, response text)
     * @throws AiRateLimitException  when the provider signals a rate-limit (HTTP 429)
     * @throws Exception             for any other provider or connectivity failure
     */
    ExternalAiRawResponse predict(String prompt, Map<String, Object> inputs) throws Exception;
}
