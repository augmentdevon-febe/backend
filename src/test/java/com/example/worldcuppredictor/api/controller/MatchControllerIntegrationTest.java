package com.example.worldcuppredictor.api.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MatchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getMatchesReturnsTopLevelArrayWhenAuthenticated() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/matches").with(user("tester")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].identifier").value("liga_mx_invierno_2026"))
                .andExpect(jsonPath("$[0].identifier").isString())
                .andExpect(jsonPath("$[0].homeTeam").isString())
                .andExpect(jsonPath("$[0].awayTeam").isString())
                .andExpect(jsonPath("$[0].matchStage").isString())
                .andExpect(jsonPath("$[0].venue").isString())
                .andExpect(jsonPath("$[0].matchDate").isString())
                .andExpect(jsonPath("$[0].matchDate").value(org.hamcrest.Matchers.matchesPattern(
                        "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:Z|[+-]\\d{2}:\\d{2})$"
                )))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        String homeTeam = JsonPath.read(response, "$[0].homeTeam");
        String awayTeam = JsonPath.read(response, "$[0].awayTeam");
        String matchStage = JsonPath.read(response, "$[0].matchStage");
        String venue = JsonPath.read(response, "$[0].venue");

        assertTrue(homeTeam.length() <= 80);
        assertTrue(awayTeam.length() <= 80);
        assertTrue(matchStage.length() <= 40);
        assertTrue(venue.length() <= 120);
    }

    @Test
    void postMatchesReturnsTopLevelArrayWhenAuthenticated() throws Exception {
        String body = """
                {
                  "identifier": "liga_mx_invierno_2026"
                }
                """;

        mockMvc.perform(post("/api/matches")
                        .with(user("tester"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].identifier").value("liga_mx_invierno_2026"));
    }

    @Test
    void postMatchesReturns400WhenIdentifierMissing() throws Exception {
        String body = "{}";

        mockMvc.perform(post("/api/matches")
                        .with(user("tester"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMatchesReturnsStrict401ErrorWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.error.message").value("Authentication required."))
                .andExpect(jsonPath("$.error.details").isArray())
                .andExpect(jsonPath("$.path").value("/api/matches"))
                .andExpect(jsonPath("$.timestamp").isString());
    }

        @Test
        void postMatchesReturnsStrict401ErrorWhenUnauthenticated() throws Exception {
                String body = """
                                {
                                    "identifier": "liga_mx_invierno_2026"
                                }
                                """;

                mockMvc.perform(post("/api/matches")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(body))
                                .andExpect(status().isUnauthorized())
                                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"))
                                .andExpect(jsonPath("$.error.message").value("Authentication required."))
                                .andExpect(jsonPath("$.error.details").isArray())
                                .andExpect(jsonPath("$.path").value("/api/matches"))
                                .andExpect(jsonPath("$.timestamp").isString());
        }
}
