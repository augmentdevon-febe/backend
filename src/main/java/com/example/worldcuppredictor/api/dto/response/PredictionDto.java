package com.example.worldcuppredictor.api.dto.response;

import com.example.worldcuppredictor.domain.entity.ResultType;

import java.time.OffsetDateTime;
import java.util.Map;

public class PredictionDto {
    private String homeTeam;
    private String awayTeam;
    private String predictedScore;
    private Integer predictedHomeGoals;
    private Integer predictedAwayGoals;
    private ResultType result;
    private Double confidence;
    private String modelVersion;
    private String providerName;
    private String providerModel;
    private String explanation;
    private Map<String, Double> factors;
    private OffsetDateTime requestedAt;

    // raw
    private String rawProviderResponse;

    public PredictionDto() {
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

    public String getPredictedScore() {
        return predictedScore;
    }

    public void setPredictedScore(String predictedScore) {
        this.predictedScore = predictedScore;
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

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
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

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Map<String, Double> getFactors() {
        return factors;
    }

    public void setFactors(Map<String, Double> factors) {
        this.factors = factors;
    }

    public OffsetDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(OffsetDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public String getRawProviderResponse() {
        return rawProviderResponse;
    }

    public void setRawProviderResponse(String rawProviderResponse) {
        this.rawProviderResponse = rawProviderResponse;
    }
}
