package com.example.worldcuppredictor.infrastructure.bootstrap;

import com.example.worldcuppredictor.domain.entity.TeamStats;
import com.example.worldcuppredictor.domain.repository.TeamStatsRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder {
    private final TeamStatsRepository teamStatsRepository;
    private final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    public DataSeeder(TeamStatsRepository teamStatsRepository) {
        this.teamStatsRepository = teamStatsRepository;
    }

    @PostConstruct
    public void seed() {
        if (teamStatsRepository.count() > 0) {
            log.info("TeamStats already seeded, skipping");
            return;
        }

        log.info("Seeding TeamStats sample data");

        List<TeamStats> samples = List.of(
                create("Argentina","ARG","CONMEBOL",1,1830.0,0.95,1.8,0.6,0.85,0.5,0.9),
                create("France","FRA","UEFA",2,1800.0,0.93,1.9,0.7,0.82,0.55,0.88),
                create("Brazil","BRA","CONMEBOL",3,1780.0,0.94,2.0,0.6,0.88,0.6,0.9),
                create("Spain","ESP","UEFA",4,1700.0,0.9,1.7,0.8,0.8,0.5,0.85)
        );

        teamStatsRepository.saveAll(samples);
        log.info("Seeded {} teams", samples.size());
    }

    private TeamStats create(String name, String code, String confed, Integer rank, Double points, Double perf, Double avgF, Double avgA, Double recent, Double attack, Double squad) {
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
        t.setDefenseScore(0.7);
        t.setSquadStrengthScore(squad);
        return t;
    }
}
