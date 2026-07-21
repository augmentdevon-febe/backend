package com.example.worldcuppredictor.infrastructure.bootstrap;

import com.example.worldcuppredictor.domain.entity.MatchCatalog;
import com.example.worldcuppredictor.domain.repository.MatchCatalogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MatchCatalogSeeder {
    private static final String MATCHES_JSON_PATH = "bootstrap/matches.json";
    private static final String LEGACY_IDENTIFIER_BACKFILL = "fifa_2026";

    private final MatchCatalogRepository matchCatalogRepository;
    private final ObjectMapper objectMapper;
    private final Logger log = LoggerFactory.getLogger(MatchCatalogSeeder.class);

    public MatchCatalogSeeder(MatchCatalogRepository matchCatalogRepository, ObjectMapper objectMapper) {
        this.matchCatalogRepository = matchCatalogRepository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void seed() {
        int backfilled = matchCatalogRepository.backfillMissingIdentifier(LEGACY_IDENTIFIER_BACKFILL);
        if (backfilled > 0) {
            log.info("Backfilled {} legacy matches with default identifier '{}'.", backfilled, LEGACY_IDENTIFIER_BACKFILL);
        }

        List<BootstrapMatch> samples = loadSamples();
        if (samples.isEmpty()) {
            log.warn("No matches found in {}. Skipping catalog sync.", MATCHES_JSON_PATH);
            return;
        }

        List<MatchCatalog> existingMatches = matchCatalogRepository.findAll();
        Map<String, MatchCatalog> existingByKey = new HashMap<>();
        for (MatchCatalog existing : existingMatches) {
            existingByKey.put(matchKey(existing.getIdentifier(), existing.getHomeTeam(), existing.getAwayTeam(), existing.getMatchStage(), existing.getVenue(), existing.getMatchDate()), existing);
        }

        Set<String> sampleKeys = samples.stream()
                .map(sample -> matchKey(sample.identifier(), sample.homeTeam(), sample.awayTeam(), sample.matchStage(), sample.venue(), sample.matchDate()))
                .collect(Collectors.toSet());

        int inserted = 0;
        int updated = 0;
        int removed = 0;

        for (BootstrapMatch sample : samples) {
            String key = matchKey(sample.identifier(), sample.homeTeam(), sample.awayTeam(), sample.matchStage(), sample.venue(), sample.matchDate());
            MatchCatalog match = existingByKey.get(key);
            if (match == null) {
                match = new MatchCatalog();
                inserted++;
            } else {
                updated++;
            }
            apply(match, sample);
            matchCatalogRepository.save(match);
        }

        for (MatchCatalog existing : existingMatches) {
            String key = matchKey(existing.getIdentifier(), existing.getHomeTeam(), existing.getAwayTeam(), existing.getMatchStage(), existing.getVenue(), existing.getMatchDate());
            if (!sampleKeys.contains(key)) {
                matchCatalogRepository.delete(existing);
                removed++;
            }
        }

        log.info("Match catalog sync complete: inserted={}, updated={}, removed={}, totalSamples={}", inserted, updated, removed, samples.size());
    }

    private List<BootstrapMatch> loadSamples() {
        try {
            var resource = new ClassPathResource(MATCHES_JSON_PATH);
            BootstrapPayload payload = objectMapper.readValue(resource.getInputStream(), BootstrapPayload.class);
            String identifier = requireIdentifier(payload.identifier());
            if (payload.matches() == null || payload.matches().isEmpty()) {
                return List.of();
            }
            List<BootstrapMatch> samples = new ArrayList<>(payload.matches().size());
            for (BootstrapMatch match : payload.matches()) {
                samples.add(new BootstrapMatch(
                        identifier,
                        match.homeTeam(),
                        match.awayTeam(),
                        match.matchStage(),
                        match.venue(),
                        match.matchDate()
                ));
            }
            return samples;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load matches catalog from classpath:" + MATCHES_JSON_PATH, e);
        }
    }

    private void apply(MatchCatalog target, BootstrapMatch sample) {
        target.setIdentifier(sample.identifier());
        target.setHomeTeam(sample.homeTeam());
        target.setAwayTeam(sample.awayTeam());
        target.setMatchStage(sample.matchStage());
        target.setVenue(sample.venue());
        target.setMatchDate(sample.matchDate());
    }

    private String matchKey(String identifier, String homeTeam, String awayTeam, String matchStage, String venue, OffsetDateTime matchDate) {
        return normalize(identifier) + "|" + normalize(homeTeam) + "|" + normalize(awayTeam) + "|" + normalize(matchStage) + "|" + normalize(venue) + "|" + matchDate;
    }

    private String requireIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalStateException("Invalid matches catalog payload: 'identifier' is required and cannot be blank");
        }
        return identifier.trim();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private record BootstrapMatch(
            String identifier,
            String homeTeam,
            String awayTeam,
            String matchStage,
            String venue,
            OffsetDateTime matchDate
    ) {
    }

        private record BootstrapPayload(
            String identifier,
            List<BootstrapMatch> matches
        ) {
        }
}
