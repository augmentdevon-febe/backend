package com.example.worldcuppredictor.domain.repository;

import com.example.worldcuppredictor.domain.entity.MatchCatalog;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchCatalogRepository extends JpaRepository<MatchCatalog, Long> {
    List<MatchCatalog> findAllByOrderByMatchDateAscIdAsc();

    List<MatchCatalog> findAllByIdentifierIgnoreCaseOrderByMatchDateAscIdAsc(String identifier);

        @Modifying
        @Transactional
        @Query("""
            update MatchCatalog m
            set m.identifier = :defaultIdentifier
            where m.identifier is null or trim(m.identifier) = ''
            """)
        int backfillMissingIdentifier(@Param("defaultIdentifier") String defaultIdentifier);

    Optional<MatchCatalog> findByHomeTeamIgnoreCaseAndAwayTeamIgnoreCaseAndMatchStageIgnoreCaseAndVenueIgnoreCaseAndMatchDate(
            String homeTeam,
            String awayTeam,
            String matchStage,
            String venue,
            OffsetDateTime matchDate
    );
}
