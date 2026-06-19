package com.example.worldcuppredictor.domain.repository;

import com.example.worldcuppredictor.domain.entity.TeamStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link TeamStats} persistence.
 *
 * <p>Provides standard CRUD operations plus a case-insensitive name lookup
 * used when validating team names in prediction requests.
 */
public interface TeamStatsRepository extends JpaRepository<TeamStats, Long> {
    /**
     * Finds a team by name, ignoring case, so that callers can match
     * "france", "France", and "FRANCE" to the same record.
     */
    Optional<TeamStats> findByTeamNameIgnoreCase(String teamName);
}
