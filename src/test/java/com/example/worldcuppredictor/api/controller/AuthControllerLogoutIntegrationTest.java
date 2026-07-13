package com.example.worldcuppredictor.api.controller;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerLogoutIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void logoutInvalidatesActiveSessionAndClearsCookie() throws Exception {
        MvcResult sessionResult = mockMvc.perform(post("/api/auth/test-session"))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession activeSession = (MockHttpSession) sessionResult.getRequest().getSession(false);
        assertNotNull(activeSession);

        MvcResult logoutResult = mockMvc.perform(post("/api/auth/logout")
                        .session(activeSession)
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logged out successfully"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"))
                .andReturn();

        List<String> setCookieHeaders = logoutResult.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertTrue(setCookieHeaders.stream().anyMatch(v -> v.contains("JSESSIONID=")), "Expected JSESSIONID clearing cookie");
        assertTrue(setCookieHeaders.stream().anyMatch(v -> v.contains("Max-Age=0")), "Expected expired cookie Max-Age=0");

        assertThrows(IllegalStateException.class, activeSession::getCreationTime);

        mockMvc.perform(get("/api/auth/session"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutWithoutSessionIsIdempotentAndReturns200() throws Exception {
        MvcResult logoutResult = mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logged out successfully"))
                .andReturn();

        List<String> setCookieHeaders = logoutResult.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertTrue(setCookieHeaders.stream().anyMatch(v -> v.contains("JSESSIONID=")), "Expected JSESSIONID clearing cookie");
    }

    @Test
    void logoutPreflightAllowsCredentialedAngularOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/logout")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    void logoutGetEndpointAlsoReturnsSuccessForManualBrowserTesting() throws Exception {
        mockMvc.perform(get("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }
}