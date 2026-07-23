package com.example.worldcuppredictor.api.controller;

import com.example.worldcuppredictor.infrastructure.exception.ServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ApiErrorContractIntegrationTest.ContractProbeController.class)
class ApiErrorContractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void serviceUnavailableReturnsStrict503Contract() throws Exception {
        mockMvc.perform(get("/api/test/service-unavailable").with(user("tester")))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("SERVICE_UNAVAILABLE"))
                .andExpect(jsonPath("$.error.message").value("Service temporarily unavailable."))
                .andExpect(jsonPath("$.error.details").isArray())
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.path").value("/api/test/service-unavailable"));
    }

    @Test
    void forbiddenReturnsStrict403Contract() throws Exception {
        mockMvc.perform(get("/api/test/admin-only").with(user("tester").roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.error.message").value("You do not have access to this resource."))
                .andExpect(jsonPath("$.error.details").isArray())
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.path").value("/api/test/admin-only"));
    }

    @Test
    void illegalArgumentReturnsStrict400Contract() throws Exception {
        mockMvc.perform(get("/api/test/bad-request").with(user("tester")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.error.message").value("Invalid request payload."))
                .andExpect(jsonPath("$.error.details").isArray())
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.path").value("/api/test/bad-request"));
    }

    @TestConfiguration
    @RestController
    @RequestMapping("/api/test")
    static class ContractProbeController {
        @GetMapping("/service-unavailable")
        public String serviceUnavailable() {
            throw new ServiceUnavailableException("Simulated outage");
        }

        @GetMapping("/bad-request")
        public String badRequest() {
            throw new IllegalArgumentException("Simulated bad request");
        }

        @GetMapping("/admin-only")
        @PreAuthorize("hasRole('ADMIN')")
        public String adminOnly() {
            return "ok";
        }
    }
}
