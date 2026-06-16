package com.example.worldcuppredictor.domain.repository;

import com.example.worldcuppredictor.domain.entity.TeamStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamStatsRepository extends JpaRepository<TeamStats, Long> {
    Optional<TeamStats> findByTeamNameIgnoreCase(String teamName);
}
