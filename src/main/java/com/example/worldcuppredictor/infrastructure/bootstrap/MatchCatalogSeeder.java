package com.example.worldcuppredictor.infrastructure.bootstrap;

import com.example.worldcuppredictor.domain.entity.MatchCatalog;
import com.example.worldcuppredictor.domain.repository.MatchCatalogRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MatchCatalogSeeder {
    private static final String MATCHES_JSON_PATH = "bootstrap/matches.json";

    private final MatchCatalogRepository matchCatalogRepository;
    private final ObjectMapper objectMapper;
    private final Logger log = LoggerFactory.getLogger(MatchCatalogSeeder.class);

    public MatchCatalogSeeder(MatchCatalogRepository matchCatalogRepository, ObjectMapper objectMapper) {
        this.matchCatalogRepository = matchCatalogRepository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void seed() {
        List<BootstrapMatch> samples = loadSamples();
        if (samples.isEmpty()) {
            log.warn("No matches found in {}. Skipping catalog sync.", MATCHES_JSON_PATH);
            return;
        }

        List<MatchCatalog> existingMatches = matchCatalogRepository.findAll();
        Map<String, MatchCatalog> existingByKey = new HashMap<>();
        for (MatchCatalog existing : existingMatches) {
            existingByKey.put(matchKey(existing.getHomeTeam(), existing.getAwayTeam(), existing.getMatchStage(), existing.getVenue(), existing.getMatchDate()), existing);
        }

        Set<String> sampleKeys = samples.stream()
                .map(sample -> matchKey(sample.homeTeam(), sample.awayTeam(), sample.matchStage(), sample.venue(), sample.matchDate()))
                .collect(Collectors.toSet());

        int inserted = 0;
        int updated = 0;
        int removed = 0;

        for (BootstrapMatch sample : samples) {
            String key = matchKey(sample.homeTeam(), sample.awayTeam(), sample.matchStage(), sample.venue(), sample.matchDate());
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
            String key = matchKey(existing.getHomeTeam(), existing.getAwayTeam(), existing.getMatchStage(), existing.getVenue(), existing.getMatchDate());
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
            return objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load matches catalog from classpath:" + MATCHES_JSON_PATH, e);
        }
    }

    private void apply(MatchCatalog target, BootstrapMatch sample) {
        target.setHomeTeam(sample.homeTeam());
        target.setAwayTeam(sample.awayTeam());
        target.setMatchStage(sample.matchStage());
        target.setVenue(sample.venue());
        target.setMatchDate(sample.matchDate());
    }

    private String matchKey(String homeTeam, String awayTeam, String matchStage, String venue, OffsetDateTime matchDate) {
        return normalize(homeTeam) + "|" + normalize(awayTeam) + "|" + normalize(matchStage) + "|" + normalize(venue) + "|" + matchDate;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private record BootstrapMatch(
            String homeTeam,
            String awayTeam,
            String matchStage,
            String venue,
            OffsetDateTime matchDate
    ) {
    }
}
