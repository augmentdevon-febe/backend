package com.example.worldcuppredictor.domain.repository;

import com.example.worldcuppredictor.domain.entity.MatchCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchCatalogRepository extends JpaRepository<MatchCatalog, Long> {
    List<MatchCatalog> findAllByOrderByMatchDateAscIdAsc();

    Optional<MatchCatalog> findByHomeTeamIgnoreCaseAndAwayTeamIgnoreCaseAndMatchStageIgnoreCaseAndVenueIgnoreCaseAndMatchDate(
            String homeTeam,
            String awayTeam,
            String matchStage,
            String venue,
            OffsetDateTime matchDate
    );
}
