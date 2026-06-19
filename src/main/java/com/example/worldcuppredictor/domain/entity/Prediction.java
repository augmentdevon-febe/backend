package com.example.worldcuppredictor.domain.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

/**
 * Persistent record of a single match prediction produced by the AI provider.
 *
 * <p>Each prediction is owned by a {@link User} and captures the full context of the
 * request (teams, stage, venue, date) alongside the AI result (score, outcome,
 * confidence, explanation) and the raw provider response for auditability.
 */
@Entity
@Table(name = "predictions")
public class Prediction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who requested this prediction. */
    @ManyToOne(optional = false)
    private User user;

    /** Name of the home team exactly as supplied in the prediction request. */
    @Column(nullable = false)
    private String homeTeam;

    /** Name of the away team exactly as supplied in the prediction request. */
    @Column(nullable = false)
    private String awayTeam;

    /** Optional tournament stage (e.g. "Group Stage", "Quarter Final"). */
    private String matchStage;

    /** Optional match venue name. */
    private String venue;

    /** Optional kick-off date and time in UTC. Null when not provided by the caller. */
    private OffsetDateTime matchDate;

    /** Number of goals predicted for the home team. */
    @Column(nullable = false)
    private Integer predictedHomeGoals;

    /** Number of goals predicted for the away team. */
    @Column(nullable = false)
    private Integer predictedAwayGoals;

    /** Categorical match outcome predicted by the AI model. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultType result;

    /** AI confidence score in the range [0, 1] where 1 is maximum confidence. */
    @Column(nullable = false)
    private Double confidenceScore;

    /** Model identifier returned by the provider (e.g. "gpt-4o-mini"). */
    private String modelVersion;

    /** Human-readable explanation of the prediction from the AI model. Stored as TEXT to support long responses. */
    @Column(columnDefinition = "TEXT")
    private String explanation;

    /** Name of the AI provider (e.g. "openai"). */
    @Column(nullable = false)
    private String providerName;

    /** Model identifier used by the provider for this prediction. */
    @Column(nullable = false)
    private String providerModel;

    /** Full raw JSON response received from the AI provider, retained for audit and debugging. */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String rawProviderResponseJson;

    /** JSON snapshot of the team feature data included in the prompt. Reserved for future ML use. */
    @Column(columnDefinition = "TEXT")
    private String featuresJson;

    /** Timestamp when the prediction request was made. Defaults to now at object creation. */
    private OffsetDateTime requestedAt = OffsetDateTime.now();

    public Prediction() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public String getMatchStage() {
        return matchStage;
    }

    public void setMatchStage(String matchStage) {
        this.matchStage = matchStage;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public OffsetDateTime getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(OffsetDateTime matchDate) {
        this.matchDate = matchDate;
    }

    public Integer getPredictedHomeGoals() {
        return predictedHomeGoals;
    }

    public void setPredictedHomeGoals(Integer predictedHomeGoals) {
        this.predictedHomeGoals = predictedHomeGoals;
    }

    public Integer getPredictedAwayGoals() {
        return predictedAwayGoals;
    }

    public void setPredictedAwayGoals(Integer predictedAwayGoals) {
        this.predictedAwayGoals = predictedAwayGoals;
    }

    public ResultType getResult() {
        return result;
    }

    public void setResult(ResultType result) {
        this.result = result;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getProviderModel() {
        return providerModel;
    }

    public void setProviderModel(String providerModel) {
        this.providerModel = providerModel;
    }

    public String getRawProviderResponseJson() {
        return rawProviderResponseJson;
    }

    public void setRawProviderResponseJson(String rawProviderResponseJson) {
        this.rawProviderResponseJson = rawProviderResponseJson;
    }

    public String getFeaturesJson() {
        return featuresJson;
    }

    public void setFeaturesJson(String featuresJson) {
        this.featuresJson = featuresJson;
    }

    public OffsetDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(OffsetDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }
}
