package com.example.worldcuppredictor.infrastructure.ai;

import com.example.worldcuppredictor.domain.entity.TeamStats;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/**
 * Builds the plain-text prompt that is sent to the AI provider to generate a match prediction.
 *
 * <p>The prompt instructs the model to act as an expert football analyst and return a strict
 * JSON payload containing predicted goals, outcome, confidence, explanation, and factor weights.
 * Team statistics loaded from the database are embedded directly in the prompt so the model
 * can ground its prediction in real data rather than relying solely on training knowledge.
 */
public class PredictionPromptBuilder {
    private static final String MATCHES_JSON_PATH = "bootstrap/matches.json";
    private static final String DEFAULT_TOURNAMENT_PROMPT_IDENTIFIER = "FIFA";
    private static final String PROMPT_PREFIX = "You are an expert in ";
    private static final String PROMPT_SUFFIX = " tournament and statistics and probabilistic football soccer analyst. Return a STRICT JSON with fields: predictedHomeGoals (int), predictedAwayGoals (int), result (HOME_WIN|AWAY_WIN|DRAW), explanation (string).\n";
    private static final String MATCH_INFO_HEADER = "Match info:\n";
    private static final String FEATURES_HEADER = "Features:\n";
    private static final String JSON_ONLY_SUFFIX = "Return only JSON. Do not add commentary.";
    private static final String UNKNOWN_TEAM = "UNKNOWN";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static volatile String cachedTournamentPromptIdentifier;

    /**
     * Assembles a structured prediction prompt for the given match.
     *
     * @param home  home team name
     * @param away  away team name
     * @param stats map of lower-case team name to {@link TeamStats} (all teams in DB)
     * @param stage optional tournament stage label (e.g. "Group Stage")
     * @param venue optional venue name
     * @param date  optional ISO-8601 kick-off timestamp string
     * @return a single string ready to send as the {@code input} field to the OpenAI Responses API
     */
    public static String build(String home, String away, Map<String, TeamStats> stats, String stage, String venue, String date) {
        TeamStats h = stats.get(home.toLowerCase(Locale.ROOT));
        TeamStats a = stats.get(away.toLowerCase(Locale.ROOT));
        String tournamentPromptIdentifier = resolveTournamentPromptIdentifier();

        StringBuilder sb = new StringBuilder();
        sb.append(PROMPT_PREFIX);
        sb.append(tournamentPromptIdentifier);
        sb.append(PROMPT_SUFFIX);
        sb.append(MATCH_INFO_HEADER);
        sb.append("home: ").append(home).append("\n");
        sb.append("away: ").append(away).append("\n");
        appendOptionalField(sb, "stage", stage);
        appendOptionalField(sb, "venue", venue);
        appendOptionalField(sb, "date", date);

        sb.append(FEATURES_HEADER);
        sb.append("home: ").append(h == null ? UNKNOWN_TEAM : h.toString()).append("\n");
        sb.append("away: ").append(a == null ? UNKNOWN_TEAM : a.toString()).append("\n");

        sb.append(JSON_ONLY_SUFFIX);

        return sb.toString();
    }

    private static void appendOptionalField(StringBuilder sb, String label, String value) {
        if (value != null) {
            sb.append(label).append(": ").append(value).append("\n");
        }
    }

    private static String resolveTournamentPromptIdentifier() {
        String cached = cachedTournamentPromptIdentifier;
        if (cached != null) {
            return cached;
        }
        synchronized (PredictionPromptBuilder.class) {
            cached = cachedTournamentPromptIdentifier;
            if (cached != null) {
                return cached;
            }
            cachedTournamentPromptIdentifier = loadTournamentPromptIdentifier();
            return cachedTournamentPromptIdentifier;
        }
    }

    private static String loadTournamentPromptIdentifier() {
        try {
            ClassPathResource resource = new ClassPathResource(MATCHES_JSON_PATH);
            JsonNode root = OBJECT_MAPPER.readTree(resource.getInputStream());
            JsonNode node = root.path("tournament_name_prompt_identifier");
            if (node.isTextual()) {
                String value = node.asText().trim();
                if (!value.isEmpty()) {
                    return value;
                }
            }
        } catch (IOException ignored) {
            // Fall back to the legacy default if catalog metadata cannot be loaded.
        }
        return DEFAULT_TOURNAMENT_PROMPT_IDENTIFIER;
    }
}
