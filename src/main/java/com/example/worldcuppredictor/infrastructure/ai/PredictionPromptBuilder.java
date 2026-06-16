package com.example.worldcuppredictor.infrastructure.ai;

import com.example.worldcuppredictor.domain.entity.TeamStats;
import java.util.Map;

public class PredictionPromptBuilder {
    public static String build(String home, String away, Map<String, TeamStats> stats, String stage, String venue, String date) {
        TeamStats h = stats.get(home.toLowerCase());
        TeamStats a = stats.get(away.toLowerCase());

        StringBuilder sb = new StringBuilder();
        sb.append("You are a football analyst. Return a STRICT JSON with fields: predictedHomeGoals (int), predictedAwayGoals (int), result (HOME_WIN|AWAY_WIN|DRAW), confidenceScore (0-1 float), explanation (string), factors (object with numeric weights summing to 1).\n");
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
