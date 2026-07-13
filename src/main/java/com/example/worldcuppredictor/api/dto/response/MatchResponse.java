package com.example.worldcuppredictor.api.dto.response;

import java.time.OffsetDateTime;

public class MatchResponse {
    private String homeTeam;
    private String awayTeam;
    private String matchStage;
    private String venue;
    private OffsetDateTime matchDate;

    public MatchResponse() {
    }

    public MatchResponse(String homeTeam, String awayTeam, String matchStage, String venue, OffsetDateTime matchDate) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.matchStage = matchStage;
        this.venue = venue;
        this.matchDate = matchDate;
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
}
