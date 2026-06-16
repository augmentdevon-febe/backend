package com.example.worldcuppredictor.api.controller;

import com.example.worldcuppredictor.domain.entity.TeamStats;
import com.example.worldcuppredictor.domain.repository.TeamStatsRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamStatsRepository repo;

    public TeamController(TeamStatsRepository repo) { this.repo = repo; }

    @GetMapping
    public List<TeamStats> list() { return repo.findAll(); }

    @GetMapping("/{teamName}")
    public TeamStats get(@PathVariable String teamName) {
        return repo.findByTeamNameIgnoreCase(teamName).orElseThrow();
    }
}
