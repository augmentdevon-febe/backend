package com.example.worldcuppredictor.domain.service;

import com.example.worldcuppredictor.api.dto.request.PredictionRequest;
import com.example.worldcuppredictor.domain.entity.ResultType;
import com.example.worldcuppredictor.domain.entity.TeamStats;
import com.example.worldcuppredictor.domain.entity.User;
import com.example.worldcuppredictor.domain.repository.PredictionRepository;
import com.example.worldcuppredictor.domain.repository.TeamStatsRepository;
import com.example.worldcuppredictor.infrastructure.ai.AiPredictionClient;
import com.example.worldcuppredictor.infrastructure.exception.ServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class PredictionServiceValidationTest {
    private PredictionRepository predictionRepository;
    private TeamStatsRepository teamStatsRepository;
    private AiPredictionClient aiClient;
    private PredictionService predictionService;

    @BeforeEach
    void setUp() throws Exception {
        predictionRepository = Mockito.mock(PredictionRepository.class);
        teamStatsRepository = Mockito.mock(TeamStatsRepository.class);
        aiClient = Mockito.mock(AiPredictionClient.class);
        predictionService = new PredictionService(predictionRepository, teamStatsRepository, aiClient);

        TeamStats homeStats = new TeamStats();
        homeStats.setTeamName("Brazil");
        homeStats.setFifaCode("BRA");
        homeStats.setConfederation("CONMEBOL");
        homeStats.setFifaRanking(1);
        homeStats.setFifaPoints(1820.0);

        TeamStats awayStats = new TeamStats();
        awayStats.setTeamName("Argentina");
        awayStats.setFifaCode("ARG");
        awayStats.setConfederation("CONMEBOL");
        awayStats.setFifaRanking(2);
        awayStats.setFifaPoints(1785.0);

        when(teamStatsRepository.findAll()).thenReturn(List.of(homeStats, awayStats));
        when(predictionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        doReturn(createMockResponse()).when(aiClient).predict(anyString(), any());
    }

    private com.example.worldcuppredictor.api.dto.response.ExternalAiRawResponse createMockResponse() {
        com.example.worldcuppredictor.api.dto.response.ExternalAiRawResponse response = new com.example.worldcuppredictor.api.dto.response.ExternalAiRawResponse();
        response.setProvider("openai");
        response.setModel("test-model");
        response.setRawText("{\"predictedHomeGoals\":2,\"predictedAwayGoals\":1,\"result\":\"HOME_WIN\",\"confidenceScore\":0.85,\"explanation\":\"test\",\"factors\":{\"ranking\":0.3}}"
        );
        return response;
    }

    @Test
    void createPredictionRejectsSameHomeAndAwayTeam() {
        PredictionRequest request = new PredictionRequest();
        request.setHomeTeam("Brazil");
        request.setAwayTeam("brazil");

        User user = new User();
        user.setGoogleSubject("sub");
        user.setEmail("test@example.com");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> predictionService.createPrediction(user, request));
        assertEquals("homeTeam and awayTeam must be different", ex.getMessage());
    }

    @Test
    void createPredictionAllowsUnknownHomeTeamWhenProviderCanRespond() throws Exception {
        PredictionRequest request = new PredictionRequest();
        request.setHomeTeam("Unknown");
        request.setAwayTeam("Argentina");

        User user = new User();
        user.setGoogleSubject("sub");
        user.setEmail("test@example.com");

        var dto = predictionService.createPrediction(user, request);

        assertEquals(2, dto.getPredictedHomeGoals());
        assertEquals(1, dto.getPredictedAwayGoals());
        assertEquals(ResultType.HOME_WIN, dto.getResult());
    }

    @Test
    void createPredictionAllowsUnknownAwayTeamWhenProviderCanRespond() throws Exception {
        PredictionRequest request = new PredictionRequest();
        request.setHomeTeam("Brazil");
        request.setAwayTeam("Unknown");

        User user = new User();
        user.setGoogleSubject("sub");
        user.setEmail("test@example.com");

        var dto = predictionService.createPrediction(user, request);

        assertEquals(2, dto.getPredictedHomeGoals());
        assertEquals(1, dto.getPredictedAwayGoals());
        assertEquals(ResultType.HOME_WIN, dto.getResult());
    }

    @Test
    void createPredictionAcceptsValidRequest() throws Exception {
        PredictionRequest request = new PredictionRequest();
        request.setHomeTeam("Brazil");
        request.setAwayTeam("Argentina");
        request.setMatchDate("2026-11-21T18:00:00Z");

        User user = new User();
        user.setGoogleSubject("sub");
        user.setEmail("test@example.com");

        var dto = predictionService.createPrediction(user, request);

        assertEquals(2, dto.getPredictedHomeGoals());
        assertEquals(1, dto.getPredictedAwayGoals());
        assertEquals(ResultType.HOME_WIN, dto.getResult());
        assertEquals("test", dto.getExplanation());
        assertEquals("openai", dto.getProviderName());
    }

    @Test
    void createPredictionWrapsProviderFailuresAsServiceUnavailable() throws Exception {
        PredictionRequest request = new PredictionRequest();
        request.setHomeTeam("Brazil");
        request.setAwayTeam("Argentina");

        User user = new User();
        user.setGoogleSubject("sub");
        user.setEmail("test@example.com");

        when(aiClient.predict(anyString(), any())).thenThrow(new IllegalStateException("OpenAI prediction failed"));

        ServiceUnavailableException ex = assertThrows(ServiceUnavailableException.class,
                () -> predictionService.createPrediction(user, request));

        assertEquals("Prediction provider unavailable", ex.getMessage());
    }

    @Test
    void findByIdForUserReturnsPredictionOnlyForItsOwner() {
        User owner = new User();
        owner.setGoogleSubject("owner-sub");
        owner.setEmail("owner@example.com");

        User other = new User();
        other.setGoogleSubject("other-sub");
        other.setEmail("other@example.com");

        com.example.worldcuppredictor.domain.entity.Prediction prediction = new com.example.worldcuppredictor.domain.entity.Prediction();
        prediction.setId(1L);
        prediction.setUser(owner);
        prediction.setHomeTeam("Brazil");
        prediction.setAwayTeam("Argentina");
        prediction.setPredictedHomeGoals(2);
        prediction.setPredictedAwayGoals(1);
        prediction.setResult(ResultType.HOME_WIN);
        prediction.setConfidenceScore(0.85);
        prediction.setProviderName("openai");
        prediction.setProviderModel("test-model");
        prediction.setRawProviderResponseJson("{}");

        when(predictionRepository.findByIdAndUser(1L, owner)).thenReturn(Optional.of(prediction));
        when(predictionRepository.findByIdAndUser(1L, other)).thenReturn(Optional.empty());

        assertTrue(predictionService.findByIdForUser(1L, owner).isPresent());
        assertTrue(predictionService.findByIdForUser(1L, other).isEmpty());
    }
}