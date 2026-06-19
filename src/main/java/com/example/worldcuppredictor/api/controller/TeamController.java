package com.example.worldcuppredictor.api.controller;

import com.example.worldcuppredictor.domain.entity.TeamStats;
import com.example.worldcuppredictor.domain.repository.TeamStatsRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only REST endpoints exposing the seeded FIFA 2026 team statistics.
 *
 * <p>These endpoints are used by consumers (e.g. a frontend autocomplete or dropdown)
 * to discover available team names before submitting a prediction request. The team
 * names returned here are the valid values accepted by {@code POST /api/predictions}.
 */
@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamStatsRepository repo;

    public TeamController(TeamStatsRepository repo) { this.repo = repo; }

    /** Returns all seeded teams. The list is unordered; clients should sort for display. */
    @GetMapping
    public List<TeamStats> list() { return repo.findAll(); }

    /** Returns a single team by name (case-insensitive). Throws 404 if not found. */
    @GetMapping("/{teamName}")
    public TeamStats get(@PathVariable String teamName) {
        return repo.findByTeamNameIgnoreCase(teamName).orElseThrow();
    }
}
