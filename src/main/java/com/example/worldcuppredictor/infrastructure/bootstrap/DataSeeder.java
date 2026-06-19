package com.example.worldcuppredictor.infrastructure.bootstrap;

import com.example.worldcuppredictor.domain.entity.TeamStats;
import com.example.worldcuppredictor.domain.repository.TeamStatsRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// This class is responsible for seeding the database with initial TeamStats data for World Cup 2026.
// It ensures that the sample data is present and up-to-date on application startup.
@Component
public class DataSeeder {
    private final TeamStatsRepository teamStatsRepository;
    private final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    public DataSeeder(TeamStatsRepository teamStatsRepository) {
        this.teamStatsRepository = teamStatsRepository;
    }

    @PostConstruct
    public void seed() {
        log.info("Ensuring TeamStats sample data is present");

        List<TeamStats> samples = buildWorldCup2026Pool();
        Set<String> sampleNames = samples.stream()
            .map(TeamStats::getTeamName)
            .map(name -> name.toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());

        int inserted = 0;
        int updated = 0;
        int removed = 0;

        for (TeamStats sample : samples) {
            var existing = teamStatsRepository.findByTeamNameIgnoreCase(sample.getTeamName());
            if (existing.isPresent()) {
                TeamStats team = existing.get();
                applySample(team, sample);
                teamStatsRepository.save(team);
                updated++;
            } else {
                teamStatsRepository.save(sample);
                inserted++;
            }
        }

        List<TeamStats> existingTeams = teamStatsRepository.findAll();
        for (TeamStats existing : existingTeams) {
            String existingName = existing.getTeamName();
            if (existingName == null) {
                continue;
            }
            if (!sampleNames.contains(existingName.toLowerCase(Locale.ROOT))) {
                teamStatsRepository.delete(existing);
                removed++;
            }
        }

        log.info("TeamStats sync complete: inserted={}, updated={}, removed={}, totalSamples={}", inserted, updated, removed, samples.size());
    }

    private List<TeamStats> buildWorldCup2026Pool() {
        return List.of(
                createByRank("Algeria", "ALG", "CAF", 1),
                createByRank("Argentina", "ARG", "CONMEBOL", 2),
                createByRank("Australia", "AUS", "AFC", 3),
                createByRank("Austria", "AUT", "UEFA", 4),
                createByRank("Belgium", "BEL", "UEFA", 5),
                createByRank("Bosnia and Herzegovina", "BSH", "UEFA", 6),
                createByRank("Brazil", "BRA", "CONMEBOL", 7),
                createByRank("Canada", "CAN", "CONCACAF", 8),
                createByRank("Cape Verde", "CAB", "XXX", 9),
                createByRank("Colombia", "COL", "CONMEBOL", 10),
                createByRank("Croatia", "CRO", "UEFA", 11),
                createByRank("Curacao", "CUR", "CONCACAF", 12),
                createByRank("Czechia", "CZE", "UEFA", 13),
                createByRank("DR Congo", "DRC", "CAF", 14),
                createByRank("Ecuador", "ECU", "CONMEBOL", 15),
                createByRank("Egypt", "EGY", "CAF", 16),
                createByRank("England", "ENG", "UEFA", 17),
                createByRank("France", "FRA", "UEFA", 18),
                createByRank("Germany", "GER", "UEFA", 19),
                createByRank("Ghana", "GHA", "CAF", 20),
                createByRank("Haiti", "HAI", "CONCACAF", 21),
                createByRank("Iran", "IRN", "AFC", 22),
                createByRank("Iraq", "IRQ", "AFC", 23),
                createByRank("Ivory Coast", "CIV", "CAF", 24),
                createByRank("Japan", "JPN", "AFC", 25),
                createByRank("Jordan", "JOR", "AFC", 26),
                createByRank("Mexico", "MEX", "CONCACAF", 27),
                createByRank("Morocco", "MAR", "CAF", 28),
                createByRank("Netherlands", "NED", "UEFA", 29),
                createByRank("New Zealand", "NZL", "OFC", 30),
                createByRank("Norway", "NOR", "UEFA", 31),
                createByRank("Panama", "PAN", "CONCACAF", 32),
                createByRank("Paraguay", "PAR", "CONMEBOL", 33),
                createByRank("Portugal", "POR", "UEFA", 34),
                createByRank("Qatar", "QAT", "AFC", 35),
                createByRank("Saudi Arabia", "KSA", "AFC", 36),
                createByRank("Scotland", "SCO", "UEFA", 37),
                createByRank("Senegal", "SEN", "CAF", 38),
                createByRank("South Africa", "RSA", "CAF", 39),
                createByRank("South Korea", "KOR", "AFC", 40),
                createByRank("Spain", "ESP", "UEFA", 41),
                createByRank("Sweden", "SWE", "UEFA", 42),
                createByRank("Switzerland", "SUI", "UEFA", 43),
                createByRank("Tunisia", "TUN", "CAF", 44),
                createByRank("Turkey", "TUR", "UEFA", 45),
                createByRank("United States", "USA", "CONCACAF", 46),
                createByRank("Uruguay", "URU", "CONMEBOL", 47),
                createByRank("Uzbekistan", "UZB", "AFC", 48)
        );
    }

    private TeamStats createByRank(String name, String code, String confed, int rank) {
        double points = 1890.0 - (rank * 10.2);
        double perf = clamp(0.98 - (rank * 0.0070), 0.58, 0.98);
        double avgGoalsFor = clamp(2.10 - (rank * 0.0200), 0.95, 2.10);
        double avgGoalsAgainst = clamp(0.50 + (rank * 0.0180), 0.50, 1.45);
        double recentForm = clamp(0.92 - (rank * 0.0065), 0.55, 0.92);
        double attack = clamp(0.90 - (rank * 0.0068), 0.52, 0.90);
        double defense = clamp(0.91 - (rank * 0.0062), 0.55, 0.91);
        double squad = clamp(0.91 - (rank * 0.0067), 0.52, 0.91);

        return create(name, code, confed, rank, points, perf, avgGoalsFor, avgGoalsAgainst, recentForm, attack, defense, squad);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private TeamStats create(String name, String code, String confed, Integer rank, Double points, Double perf, Double avgF, Double avgA, Double recent, Double attack, Double defense, Double squad) {
        TeamStats t = new TeamStats();
        t.setTeamName(name);
        t.setFifaCode(code);
        t.setConfederation(confed);
        t.setFifaRanking(rank);
        t.setFifaPoints(points);
        t.setWorldCupPerformanceScore(perf);
        t.setAvgGoalsFor(avgF);
        t.setAvgGoalsAgainst(avgA);
        t.setRecentFormScore(recent);
        t.setAttackScore(attack);
        t.setDefenseScore(defense);
        t.setSquadStrengthScore(squad);
        return t;
    }

    private void applySample(TeamStats target, TeamStats sample) {
        target.setFifaCode(sample.getFifaCode());
        target.setConfederation(sample.getConfederation());
        target.setFifaRanking(sample.getFifaRanking());
        target.setFifaPoints(sample.getFifaPoints());
        target.setWorldCupPerformanceScore(sample.getWorldCupPerformanceScore());
        target.setAvgGoalsFor(sample.getAvgGoalsFor());
        target.setAvgGoalsAgainst(sample.getAvgGoalsAgainst());
        target.setRecentFormScore(sample.getRecentFormScore());
        target.setAttackScore(sample.getAttackScore());
        target.setDefenseScore(sample.getDefenseScore());
        target.setSquadStrengthScore(sample.getSquadStrengthScore());
        target.setUpdatedAt(OffsetDateTime.now());
    }
}
