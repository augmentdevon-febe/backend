package com.example.worldcuppredictor.infrastructure.ai;

import com.example.worldcuppredictor.domain.entity.TeamStats;
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
        TeamStats h = stats.get(home.toLowerCase());
        TeamStats a = stats.get(away.toLowerCase());

        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert in FIFA tournaments and statistics and probabilistic football analyst. Return a STRICT JSON with fields: predictedHomeGoals (int), predictedAwayGoals (int), result (HOME_WIN|AWAY_WIN|DRAW), confidenceScore (0-1 float), explanation (string), factors (object with numeric weights summing to 1).\n");
        sb.append("Match info:\n");
        sb.append("home: ").append(home).append("\n");
        sb.append("away: ").append(away).append("\n");
        if (stage != null) sb.append("stage: ").append(stage).append("\n");
        if (venue != null) sb.append("venue: ").append(venue).append("\n");
        if (date != null) sb.append("date: ").append(date).append("\n");

        sb.append("Features:\n");
        sb.append("home: ").append(h == null ? "UNKNOWN" : h.toString()).append("\n");
        sb.append("away: ").append(a == null ? "UNKNOWN" : a.toString()).append("\n");

        sb.append("Return only JSON. Do not add commentary.");

        return sb.toString();
    }
}
