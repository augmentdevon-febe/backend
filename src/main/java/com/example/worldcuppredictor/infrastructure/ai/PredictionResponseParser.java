package com.example.worldcuppredictor.infrastructure.ai;

import com.example.worldcuppredictor.api.dto.response.ExternalAiRawResponse;
import com.example.worldcuppredictor.api.dto.response.PredictionDto;
import com.example.worldcuppredictor.domain.entity.ResultType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Parses the raw text response from an AI provider into a structured {@link PredictionDto}.
 *
 * <p>Different AI providers and API versions return JSON in different shapes. This parser
 * applies a multi-strategy approach to locate the prediction payload:
 * <ol>
 *   <li>Attempts to parse the top-level text as JSON directly.</li>
 *   <li>Walks well-known envelope structures: {@code outputs[]}, {@code output[]},
 *       {@code choices[]}, {@code outputText}, {@code output_text}, {@code content},
 *       {@code message}.</li>
 *   <li>Recursively searches all string nodes for embedded JSON as a last resort.</li>
 *   <li>Attempts to extract the first {@code {...}} block from free-form text.</li>
 * </ol>
 *
 * <p>A node is recognised as a prediction payload when it contains at least one of
 * {@code predictedHomeGoals} / {@code predictedAwayGoals} AND a {@code result} field.
 */
public class PredictionResponseParser {
    private static final Logger log = LoggerFactory.getLogger(PredictionResponseParser.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Parses a raw AI provider response into a {@link PredictionDto}.
     *
     * @param raw the raw response wrapper containing provider name, model, and raw text
     * @return a populated {@link PredictionDto}
     * @throws IllegalArgumentException if the response is null, empty, or no prediction
     *         payload can be located
     */
    public static PredictionDto parse(ExternalAiRawResponse raw) throws Exception {
        String text = raw.getRawText();
        if (text == null) throw new IllegalArgumentException("Empty provider response");

        JsonNode root = parseJson(text);
        JsonNode payload = findPredictionPayload(root);

        PredictionDto dto = new PredictionDto();
        dto.setProviderName(raw.getProvider());
        dto.setProviderModel(raw.getModel());
        dto.setRawProviderResponse(text);

        dto.setPredictedHomeGoals(payload.path("predictedHomeGoals").asInt(0));
        dto.setPredictedAwayGoals(payload.path("predictedAwayGoals").asInt(0));
        dto.setConfidence(payload.path("confidenceScore").asDouble(0.0));
        dto.setExplanation(payload.path("explanation").asText(null));

        String res = payload.path("result").asText(null);
        if ("HOME_WIN".equalsIgnoreCase(res) || "HOMEWIN".equalsIgnoreCase(res)) {
            dto.setResult(ResultType.HOME_WIN);
        } else if ("AWAY_WIN".equalsIgnoreCase(res) || "AWAYWIN".equalsIgnoreCase(res)) {
            dto.setResult(ResultType.AWAY_WIN);
        } else {
            dto.setResult(ResultType.DRAW);
        }

        JsonNode factors = payload.path("factors");
        if (!factors.isMissingNode() && !factors.isNull()) {
            dto.setFactors(MAPPER.convertValue(factors, HashMap.class));
        }

        dto.setPredictedScore(dto.getPredictedHomeGoals() + "-" + dto.getPredictedAwayGoals());
        dto.setModelVersion(raw.getModel());

        return dto;
    }

    /** Attempts to parse {@code text} as JSON; falls back to {@link #parseJsonFromText} on failure. */
    private static JsonNode parseJson(String text) throws Exception {
        try {
            return MAPPER.readTree(text);
        } catch (Exception ex) {
            return parseJsonFromText(text);
        }
    }

    /**
     * Walks the JSON tree looking for a node that satisfies {@link #isPredictionPayload}.
     * Tries known envelope keys before falling back to a full recursive search.
     */
    private static JsonNode findPredictionPayload(JsonNode root) throws Exception {
        if (isPredictionPayload(root)) {
            return root;
        }

        JsonNode candidate = null;

        if (root.has("outputs") && root.path("outputs").isArray()) {
            for (JsonNode item : root.path("outputs")) {
                candidate = tryExtractPrediction(item);
                if (candidate != null) return candidate;
            }
        }

        if (root.has("output") && root.path("output").isArray()) {
            for (JsonNode item : root.path("output")) {
                candidate = tryExtractPrediction(item);
                if (candidate != null) return candidate;
            }
        }

        if (root.has("choices") && root.path("choices").isArray()) {
            for (JsonNode choice : root.path("choices")) {
                candidate = tryExtractPrediction(choice);
                if (candidate != null) return candidate;
            }
        }

        if (root.has("outputText")) {
            return parseJsonFromText(root.path("outputText").asText(""));
        }

        if (root.has("output_text")) {
            return parseJsonFromText(root.path("output_text").asText(""));
        }

        if (root.has("content")) {
            candidate = tryExtractPrediction(root.path("content"));
            if (candidate != null) return candidate;
        }

        if (root.has("message")) {
            candidate = tryExtractPrediction(root.path("message"));
            if (candidate != null) return candidate;
        }

        candidate = searchForPredictionJson(root);
        if (candidate != null) {
            return candidate;
        }

        throw new IllegalArgumentException("Provider returned unexpected response shape; unable to locate prediction payload");
    }

    /**
     * Recursively tries to extract a prediction payload from {@code node}.
     * Handles textual nodes (by parsing as JSON), objects with {@code content} / {@code message} /
     * {@code output_text} / {@code text} / {@code parts} keys, and arrays.
     */
    private static JsonNode tryExtractPrediction(JsonNode node) throws Exception {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        if (isPredictionPayload(node)) {
            return node;
        }

        if (node.isTextual()) {
            return parseJsonFromText(node.asText());
        }

        if (node.isObject()) {
            if (node.has("content")) {
                JsonNode content = node.path("content");
                if (content.isTextual()) {
                    return parseJsonFromText(content.asText());
                }
                JsonNode result = tryExtractPrediction(content);
                if (result != null) {
                    return result;
                }
            }

            if (node.has("message")) {
                JsonNode message = node.path("message");
                JsonNode result = tryExtractPrediction(message);
                if (result != null) {
                    return result;
                }
            }

            if (node.has("output_text")) {
                return parseJsonFromText(node.path("output_text").asText());
            }

            if (node.has("text")) {
                JsonNode text = node.path("text");
                if (text.isTextual()) {
                    return parseJsonFromText(text.asText());
                }
            }

            if (node.has("parts") && node.path("parts").isArray()) {
                for (JsonNode part : node.path("parts")) {
                    JsonNode result = tryExtractPrediction(part);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }

        if (node.isArray()) {
            for (JsonNode element : node) {
                JsonNode result = tryExtractPrediction(element);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    /** Deep-searches every descendant node for embedded JSON that is a prediction payload. */
    private static JsonNode searchForPredictionJson(JsonNode node) throws Exception {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        if (node.isTextual()) {
            return parseJsonFromText(node.asText());
        }

        if (node.isObject()) {
            for (JsonNode child : node) {
                JsonNode result = searchForPredictionJson(child);
                if (result != null) {
                    return result;
                }
            }
        }

        if (node.isArray()) {
            for (JsonNode element : node) {
                JsonNode result = searchForPredictionJson(element);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    /** Returns {@code true} when {@code node} contains goal and result fields that identify it as a prediction payload. */
    private static boolean isPredictionPayload(JsonNode node) {
        return node != null && node.isObject()
                && (node.has("predictedHomeGoals") || node.has("predictedAwayGoals"))
                && node.has("result");
    }

    /**
     * Parses {@code text} as JSON. If direct parsing fails, attempts to extract the first
     * {@code {...}} block from the text before throwing.
     *
     * @throws IllegalArgumentException when no valid JSON object can be extracted
     */
    private static JsonNode parseJsonFromText(String text) throws Exception {
        if (text == null) {
            throw new IllegalArgumentException("Provider returned non-JSON response and no JSON could be extracted");
        }

        try {
            return MAPPER.readTree(text);
        } catch (Exception ex) {
            int idx = text.indexOf('{');
            int last = text.lastIndexOf('}');
            if (idx >= 0 && last > idx) {
                String sub = text.substring(idx, last + 1);
                return MAPPER.readTree(sub);
            }
            throw new IllegalArgumentException("Provider returned non-JSON response and no JSON could be extracted", ex);
        }
    }
}
