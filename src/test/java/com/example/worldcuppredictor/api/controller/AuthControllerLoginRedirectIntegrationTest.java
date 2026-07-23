package com.example.worldcuppredictor.api.controller;

import com.example.worldcuppredictor.infrastructure.security.AuthRedirectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerLoginRedirectIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginStoresRedirectUriByPriorityAndStartsOAuthFlow() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/login")
                        .param("redirect_uri", "http://localhost:4200/login")
                        .param("redirectUrl", "http://localhost:4200/should-not-win")
                        .param("returnUrl", "http://localhost:4200/also-lower-priority"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/oauth2/authorization/google"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        assertEquals(
                "http://localhost:4200/login",
                session.getAttribute(AuthRedirectService.POST_AUTH_REDIRECT_SESSION_KEY)
        );
    }

    @Test
    void loginWithoutRedirectParamsStoresFallback() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/oauth2/authorization/google"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        assertEquals(
                AuthRedirectService.FALLBACK_REDIRECT,
                session.getAttribute(AuthRedirectService.POST_AUTH_REDIRECT_SESSION_KEY)
        );
    }

    @Test
    void loginWithInvalidExternalRedirectStoresFallback() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/login")
                        .param("redirect_uri", "https://evil.example.com/login")
                        .param("redirectUrl", "https://evil.example.com/alt")
                        .param("returnUrl", "https://evil.example.com/return"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/oauth2/authorization/google"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        assertEquals(
                AuthRedirectService.FALLBACK_REDIRECT,
                session.getAttribute(AuthRedirectService.POST_AUTH_REDIRECT_SESSION_KEY)
        );
    }

        @Test
        void googleAuthorizationRequestForcesAccountSelection() throws Exception {
                MvcResult result = mockMvc.perform(get("/oauth2/authorization/google"))
                                .andExpect(status().is3xxRedirection())
                                .andReturn();

                String redirectUrl = result.getResponse().getRedirectedUrl();
                assertNotNull(redirectUrl);
                assertTrue(redirectUrl.contains("prompt=select_account"));
        }

        @Test
        void switchAccountClearsSessionAndPreservesPredictRedirect() throws Exception {
                MockHttpSession existingSession = new MockHttpSession();
                existingSession.setAttribute("marker", "keep-me-out");

                MvcResult result = mockMvc.perform(get("/api/auth/switch-account")
                                                .session(existingSession)
                                                .param("redirect_uri", "http://localhost:4200/predict"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/oauth2/authorization/google"))
                                .andReturn();

                MockHttpSession newSession = (MockHttpSession) result.getRequest().getSession(false);
                assertNotNull(newSession);
                assertNotEquals(existingSession.getId(), newSession.getId());
                assertEquals(
                                "http://localhost:4200/predict",
                                newSession.getAttribute(AuthRedirectService.POST_AUTH_REDIRECT_SESSION_KEY)
                );
                assertTrue(result.getResponse().getHeaders(HttpHeaders.SET_COOKIE).stream().anyMatch(header -> header.startsWith("JSESSIONID=")));
                assertTrue(result.getResponse().getHeaders(HttpHeaders.SET_COOKIE).stream().anyMatch(header -> header.startsWith("SESSION=")));
        }
}