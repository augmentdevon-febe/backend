package com.example.worldcuppredictor.infrastructure.ai;

import com.example.worldcuppredictor.api.dto.response.ExternalAiRawResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

/**
 * Default {@link AiPredictionClient} implementation that calls the OpenAI Responses API
 * ({@code POST https://api.openai.com/v1/responses}).
 *
 * <p>Configuration is driven by application properties and environment variables:
 * <ul>
 *   <li>{@code app.ai.model} — model identifier (default: {@code gpt-4o-mini})</li>
 *   <li>{@code app.ai.provider} — provider label stored in prediction records (default: {@code openai})</li>
 *   <li>{@code OPENAI_API_KEY} — Bearer token. A startup warning is logged when absent.</li>
 * </ul>
 *
 * <p>HTTP 429 responses from OpenAI are surfaced as {@link AiRateLimitException}.
 * All other non-2xx responses and connectivity failures throw {@link IllegalStateException}.
 */
@Service
@Primary
public class OpenAiPredictionClient implements AiPredictionClient {
    private static final Logger log = LoggerFactory.getLogger(OpenAiPredictionClient.class);

    private final WebClient webClient;
    private final String modelId;
    private final String providerName;

    public OpenAiPredictionClient(WebClient.Builder webClientBuilder,
                                  @Value("${app.ai.model:openai-default}") String modelId,
                                  @Value("${app.ai.provider:openai}") String providerName,
                                  @Value("${OPENAI_API_KEY:}") String apiKey) {
        this.modelId = modelId;
        this.providerName = providerName;

        WebClient.Builder builder = webClientBuilder.baseUrl("https://api.openai.com/v1");
        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
        } else {
            log.warn("OPENAI_API_KEY is not set; AI predictions may fail if the endpoint requires authentication.");
        }

        this.webClient = builder.build();
    }

    /**
     * Sends the prompt to the OpenAI Responses API and returns the raw response body.
     *
     * @param prompt  the full plain-text prediction prompt built by {@link com.example.worldcuppredictor.infrastructure.ai.PredictionPromptBuilder}
     * @param inputs  additional payload fields merged into the request body (may be empty)
     * @return wrapper containing the raw response text, provider name, and model identifier
     * @throws AiRateLimitException  when OpenAI returns HTTP 429
     * @throws IllegalStateException for any other provider error or connectivity failure
     */
    @Override
    public ExternalAiRawResponse predict(String prompt, Map<String, Object> inputs) throws Exception {
        Map<String, Object> payload = new HashMap<>(inputs != null ? inputs : Map.of());
        payload.put("model", modelId);
        payload.put("input", prompt);

        String responseBody;
        try {
            responseBody = webClient.post()
                    .uri("/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException ex) {
            String body = ex.getResponseBodyAsString();
            log.error("OpenAI API returned error {}: {}", ex.getRawStatusCode(), body, ex);
            if (ex.getStatusCode().value() == 429) {
                throw new AiRateLimitException("OpenAI rate limit exceeded: " + body, ex);
            }
            throw new IllegalStateException("OpenAI prediction failed: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Unable to call OpenAI API", ex);
            throw new IllegalStateException("OpenAI prediction failed", ex);
        }

        ExternalAiRawResponse raw = new ExternalAiRawResponse();
        raw.setProvider(providerName);
        raw.setModel(modelId);
        raw.setRawText(responseBody != null ? responseBody : "");
        return raw;
    }
}
