package com.example.worldcuppredictor.domain.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "team_stats")
public class TeamStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String teamName;

    @Column(nullable = false, unique = true)
    private String fifaCode;

    @Column(nullable = false)
    private String confederation;

    @Column(nullable = false)
    private Integer fifaRanking;
    private Double fifaPoints;
    private Double worldCupPerformanceScore;
    private Double avgGoalsFor;
    private Double avgGoalsAgainst;
    private Double recentFormScore;
    private Double attackScore;
    private Double defenseScore;
    private Double squadStrengthScore;

    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public TeamStats() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getFifaCode() {
        return fifaCode;
    }

    public void setFifaCode(String fifaCode) {
        this.fifaCode = fifaCode;
    }

    public String getConfederation() {
        return confederation;
    }

    public void setConfederation(String confederation) {
        this.confederation = confederation;
    }

    public Integer getFifaRanking() {
        return fifaRanking;
    }

    public void setFifaRanking(Integer fifaRanking) {
        this.fifaRanking = fifaRanking;
    }

    public Double getFifaPoints() {
        return fifaPoints;
    }

    public void setFifaPoints(Double fifaPoints) {
        this.fifaPoints = fifaPoints;
    }

    public Double getWorldCupPerformanceScore() {
        return worldCupPerformanceScore;
    }

    public void setWorldCupPerformanceScore(Double worldCupPerformanceScore) {
        this.worldCupPerformanceScore = worldCupPerformanceScore;
    }

    public Double getAvgGoalsFor() {
        return avgGoalsFor;
    }

    public void setAvgGoalsFor(Double avgGoalsFor) {
        this.avgGoalsFor = avgGoalsFor;
    }

    public Double getAvgGoalsAgainst() {
        return avgGoalsAgainst;
    }

    public void setAvgGoalsAgainst(Double avgGoalsAgainst) {
        this.avgGoalsAgainst = avgGoalsAgainst;
    }

    public Double getRecentFormScore() {
        return recentFormScore;
    }

    public void setRecentFormScore(Double recentFormScore) {
        this.recentFormScore = recentFormScore;
    }

    public Double getAttackScore() {
        return attackScore;
    }

    public void setAttackScore(Double attackScore) {
        this.attackScore = attackScore;
    }

    public Double getDefenseScore() {
        return defenseScore;
    }

    public void setDefenseScore(Double defenseScore) {
        this.defenseScore = defenseScore;
    }

    public Double getSquadStrengthScore() {
        return squadStrengthScore;
    }

    public void setSquadStrengthScore(Double squadStrengthScore) {
        this.squadStrengthScore = squadStrengthScore;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
