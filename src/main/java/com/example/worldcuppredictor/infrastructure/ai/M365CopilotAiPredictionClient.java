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

@Service
@Primary
public class M365CopilotAiPredictionClient implements AiPredictionClient {
    private static final Logger log = LoggerFactory.getLogger(M365CopilotAiPredictionClient.class);

    private final WebClient webClient;
    private final String modelId;
    private final String providerName;

    public M365CopilotAiPredictionClient(WebClient.Builder webClientBuilder,
                                          @Value("${app.ai.model:m365-copilot-default}") String modelId,
                                          @Value("${app.ai.provider:m365-copilot}") String providerName,
                                          @Value("${M365_COPILOT_API_TOKEN:}") String apiToken) {
        this.modelId = modelId;
        this.providerName = providerName;

        WebClient.Builder builder = webClientBuilder.baseUrl("https://api.m365copilot.microsoft.com");
        if (apiToken != null && !apiToken.isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken);
        } else {
            log.warn("M365_COPILOT_API_TOKEN is not set; AI predictions may fail if the endpoint requires authentication.");
        }

        this.webClient = builder.build();
    }

    @Override
    public ExternalAiRawResponse predict(String prompt, Map<String, Object> inputs) throws Exception {
        Map<String, Object> payload = new HashMap<>(inputs != null ? inputs : Map.of());
        payload.put("inputs", prompt);

        String responseBody;
        try {
            responseBody = webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/models/{model}").build(modelId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("M365 Copilot API returned error {}: {}", ex.getRawStatusCode(), ex.getResponseBodyAsString(), ex);
            throw new IllegalStateException("M365 Copilot prediction failed: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Unable to call M365 Copilot API", ex);
            throw new IllegalStateException("M365 Copilot prediction failed", ex);
        }

        ExternalAiRawResponse raw = new ExternalAiRawResponse();
        raw.setProvider(providerName);
        raw.setModel(modelId);
        raw.setRawText(responseBody != null ? responseBody : "");
        return raw;
    }
}
