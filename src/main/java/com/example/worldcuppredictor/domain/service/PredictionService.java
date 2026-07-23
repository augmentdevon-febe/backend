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
import com.example.worldcuppredictor.infrastructure.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Core application service responsible for the end-to-end prediction workflow.
 *
 * <ol>
 *   <li>Validates the incoming {@link PredictionRequest} (non-blank teams, different teams,
 *       both teams known, optional ISO-8601 match date).</li>
 *   <li>Loads all {@link TeamStats} from the database and builds a structured prompt via
 *       {@link PredictionPromptBuilder}.</li>
 *   <li>Calls the configured {@link AiPredictionClient} (OpenAI by default) with the prompt.</li>
 *   <li>Parses the raw AI response via {@link PredictionResponseParser} into a {@link PredictionDto}.</li>
 *   <li>Persists the result as a {@link Prediction} entity linked to the requesting user.</li>
 * </ol>
 */
@Service
public class PredictionService {
    /**
     * Guard-length used when the database {@code explanation} column has not yet been
     * migrated from VARCHAR(255) to TEXT. Explanations exceeding this length are silently
     * truncated with a WARN log to prevent SQL 22001 insert failures on legacy schemas.
     */
    private static final int LEGACY_EXPLANATION_MAX_LENGTH = 255;

    private final PredictionRepository predictionRepository;
    private final TeamStatsRepository teamStatsRepository;
    private final AiPredictionClient aiClient;
    private final Logger log = LoggerFactory.getLogger(PredictionService.class);

    public PredictionService(PredictionRepository predictionRepository, TeamStatsRepository teamStatsRepository, AiPredictionClient aiClient) {
        this.predictionRepository = predictionRepository;
        this.teamStatsRepository = teamStatsRepository;
        this.aiClient = aiClient;
    }

    /**
     * Validates the request, calls the AI provider, persists the result, and returns a DTO.
     *
     * @param user the authenticated user making the request
     * @param req  validated prediction request containing team names and optional match context
     * @return a populated {@link PredictionDto} with the AI prediction and provider metadata
     * @throws IllegalArgumentException if validation fails (unknown team, same teams, bad date)
     */
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

        ExternalAiRawResponse raw;
        PredictionDto dto;
        try {
            String prompt = PredictionPromptBuilder.build(req.getHomeTeam(), req.getAwayTeam(), stats, req.getMatchStage(), req.getVenue(), req.getMatchDate());
            raw = aiClient.predict(prompt, Map.of());
            dto = PredictionResponseParser.parse(raw);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            log.error("Prediction provider failed for {} vs {}", req.getHomeTeam(), req.getAwayTeam(), ex);
            throw new ServiceUnavailableException("Prediction provider unavailable", ex);
        }

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
        p.setExplanation(trimExplanationForLegacySchema(dto.getExplanation()));
        p.setProviderName(dto.getProviderName());
        p.setProviderModel(dto.getProviderModel());
        p.setRawProviderResponseJson(dto.getRawProviderResponse());
        p.setFeaturesJson("{}");
        p.setRequestedAt(OffsetDateTime.now());

        predictionRepository.save(p);

        return dto;
    }

    /**
     * Returns a page of predictions belonging to {@code user}, ordered by
     * match date ascending (nulls last), then by requested-at descending.
     */
    public Page<Prediction> listForUser(User user, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by(
                Sort.Order.asc("matchDate").nullsLast(),
                Sort.Order.desc("requestedAt")
            )
        );
        return predictionRepository.findByUser(user, sortedPageable);
    }

    /**
     * Returns the prediction with the given {@code id} only when it belongs to {@code user}.
     * Returns empty when the prediction does not exist or is owned by a different user.
     */
    public java.util.Optional<Prediction> findByIdForUser(Long id, User user) {
        return predictionRepository.findByIdAndUser(id, user);
    }

    /**
     * Truncates the explanation to {@link #LEGACY_EXPLANATION_MAX_LENGTH} characters when
     * necessary so that inserts succeed on databases where the column is still VARCHAR(255).
     */
    private String trimExplanationForLegacySchema(String explanation) {
        if (explanation == null) {
            return null;
        }
        if (explanation.length() <= LEGACY_EXPLANATION_MAX_LENGTH) {
            return explanation;
        }
        log.warn("Trimming explanation from {} to {} chars for DB compatibility", explanation.length(), LEGACY_EXPLANATION_MAX_LENGTH);
        return explanation.substring(0, LEGACY_EXPLANATION_MAX_LENGTH);
    }

}
