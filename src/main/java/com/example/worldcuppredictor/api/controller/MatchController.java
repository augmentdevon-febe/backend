package com.example.worldcuppredictor.api.controller;

import com.example.worldcuppredictor.api.dto.response.MatchResponse;
import com.example.worldcuppredictor.domain.repository.MatchCatalogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only endpoint for predictable World Cup matches used by frontend dropdowns.
 */
@RestController
@RequestMapping("/api/matches")
@Tag(name = "Matches", description = "Match catalog for prediction flow")
public class MatchController {
    private final MatchCatalogRepository matchCatalogRepository;

    public MatchController(MatchCatalogRepository matchCatalogRepository) {
        this.matchCatalogRepository = matchCatalogRepository;
    }

    @Operation(
            operationId = "getMatches",
            summary = "Get list of predictable matches",
            description = "Returns a top-level JSON array of match objects.",
            security = {@SecurityRequirement(name = "cookieAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Matches loaded successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = MatchResponse.class)))),
            @ApiResponse(responseCode = "401", description = "No valid authenticated session"),
            @ApiResponse(responseCode = "403", description = "Authenticated but forbidden"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
            @ApiResponse(responseCode = "503", description = "Service unavailable")
    })
    @GetMapping
    public List<MatchResponse> list() {
        return matchCatalogRepository.findAllByOrderByMatchDateAscIdAsc().stream()
                .map(m -> new MatchResponse(
                        m.getHomeTeam(),
                        m.getAwayTeam(),
                        m.getMatchStage(),
                        m.getVenue(),
                        m.getMatchDate()
                ))
                .toList();
    }
}
