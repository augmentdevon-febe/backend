package com.example.worldcuppredictor.domain.service;

import com.example.worldcuppredictor.api.dto.request.PredictionRequest;
import com.example.worldcuppredictor.api.dto.response.ExternalAiRawResponse;
import com.example.worldcuppredictor.api.dto.response.PredictionDto;
import com.example.worldcuppredictor.domain.entity.Prediction;
import com.example.worldcuppredictor.domain.entity.TeamStats;
import com.example.worldcuppredictor.domain.entity.User;
import com.example.worldcuppredictor.domain.repository.PredictionRepository;
import com.example.worldcuppredictor.domain.repository.TeamStatsRepository;
import com.example.worldcuppredictor.infrastructure.ai.AiPredictionClient;
import com.example.worldcuppredictor.infrastructure.ai.PredictionPromptBuilder;
import com.example.worldcuppredictor.infrastructure.ai.PredictionResponseParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PredictionService {
    private final PredictionRepository predictionRepository;
    private final TeamStatsRepository teamStatsRepository;
    private final AiPredictionClient aiClient;
    private final Logger log = LoggerFactory.getLogger(PredictionService.class);

    public PredictionService(PredictionRepository predictionRepository, TeamStatsRepository teamStatsRepository, AiPredictionClient aiClient) {
        this.predictionRepository = predictionRepository;
        this.teamStatsRepository = teamStatsRepository;
        this.aiClient = aiClient;
    }

    public PredictionDto createPrediction(User user, PredictionRequest req) throws Exception {
        if (req.getHomeTeam() == null || req.getHomeTeam().isBlank()) {
            throw new IllegalArgumentException("homeTeam is required");
        }
        if (req.getAwayTeam() == null || req.getAwayTeam().isBlank()) {
            throw new IllegalArgumentException("awayTeam is required");
        }
        if (req.getHomeTeam().equalsIgnoreCase(req.getAwayTeam())) {
            throw new IllegalArgumentException("homeTeam and awayTeam must be different");
        }

        Map<String, TeamStats> stats = teamStatsRepository.findAll().stream()
                .collect(Collectors.toMap(ts -> ts.getTeamName().toLowerCase(), ts -> ts));

        if (!stats.containsKey(req.getHomeTeam().toLowerCase())) {
            throw new IllegalArgumentException("Unknown home team: " + req.getHomeTeam());
        }
        if (!stats.containsKey(req.getAwayTeam().toLowerCase())) {
            throw new IllegalArgumentException("Unknown away team: " + req.getAwayTeam());
        }

        OffsetDateTime parsedDate = null;
        if (req.getMatchDate() != null && !req.getMatchDate().isBlank()) {
            try {
                parsedDate = OffsetDateTime.parse(req.getMatchDate());
            } catch (Exception ex) {
                throw new IllegalArgumentException("matchDate must be a valid ISO-8601 timestamp");
            }
        }

        String prompt = PredictionPromptBuilder.build(req.getHomeTeam(), req.getAwayTeam(), stats, req.getMatchStage(), req.getVenue(), req.getMatchDate());

        ExternalAiRawResponse raw = aiClient.predict(prompt, Map.of());

        var dto = PredictionResponseParser.parse(raw);
        dto.setHomeTeam(req.getHomeTeam());
        dto.setAwayTeam(req.getAwayTeam());
        dto.setProviderName(raw.getProvider());
        dto.setProviderModel(raw.getModel());
        dto.setRequestedAt(OffsetDateTime.now());

        Prediction p = new Prediction();
        p.setUser(user);
        p.setHomeTeam(req.getHomeTeam());
        p.setAwayTeam(req.getAwayTeam());
        p.setMatchStage(req.getMatchStage());
        p.setVenue(req.getVenue());
        p.setMatchDate(parsedDate);
        p.setPredictedHomeGoals(dto.getPredictedHomeGoals());
        p.setPredictedAwayGoals(dto.getPredictedAwayGoals());
        p.setResult(dto.getResult());
        p.setConfidenceScore(dto.getConfidence());
        p.setModelVersion(dto.getModelVersion());
        p.setExplanation(dto.getExplanation());
        p.setProviderName(dto.getProviderName());
        p.setProviderModel(dto.getProviderModel());
        p.setRawProviderResponseJson(dto.getRawProviderResponse());
        p.setFeaturesJson("{}");
        p.setRequestedAt(OffsetDateTime.now());

        predictionRepository.save(p);

        return dto;
    }

    public Page<Prediction> listForUser(User user, Pageable pageable) {
        return predictionRepository.findByUserOrderByRequestedAtDesc(user, pageable);
    }

    public java.util.Optional<Prediction> findByIdForUser(Long id, User user) {
        return predictionRepository.findByIdAndUser(id, user);
    }

}
