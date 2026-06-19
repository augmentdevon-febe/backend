package com.example.worldcuppredictor.domain.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

/**
 * Statistical profile for a single national team, used as context when building the AI prediction prompt.
 *
 * <p>Records are seeded on startup by {@code DataSeeder} from the FIFA 2026 World Cup pool
 * and upserted on every restart so that stats stay current. All numeric scores are normalised
 * to the range [0, 1] unless noted otherwise.
 */
@Entity
@Table(name = "team_stats")
public class TeamStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Full English team name used in API requests and AI prompts (e.g. "South Korea"). */
    @Column(nullable = false, unique = true)
    private String teamName;

    /** Three-letter FIFA country code (e.g. "KOR"). */
    @Column(nullable = false, unique = true)
    private String fifaCode;

    /** FIFA confederation (UEFA, CONMEBOL, CONCACAF, CAF, AFC, OFC). */
    @Column(nullable = false)
    private String confederation;

    /** Current FIFA world ranking position (lower is better). */
    @Column(nullable = false)
    private Integer fifaRanking;

    /** FIFA ranking points total. */
    private Double fifaPoints;

    /** Normalised score [0, 1] reflecting historical World Cup results. */
    private Double worldCupPerformanceScore;

    /** Average goals scored per match over recent competitive fixtures. */
    private Double avgGoalsFor;

    /** Average goals conceded per match over recent competitive fixtures. */
    private Double avgGoalsAgainst;

    /** Normalised score [0, 1] reflecting recent match form. */
    private Double recentFormScore;

    /** Normalised attacking ability score [0, 1]. */
    private Double attackScore;

    /** Normalised defensive ability score [0, 1]. */
    private Double defenseScore;

    /** Normalised overall squad quality score [0, 1]. */
    private Double squadStrengthScore;

    /** Timestamp when this record was first inserted. */
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /** Timestamp of the most recent upsert by {@code DataSeeder}. */
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
