package com.example.worldcuppredictor.infrastructure.security;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuthRedirectSuccessHandlerTest {

    private final AuthRedirectService authRedirectService = new AuthRedirectService();
    private final AuthRedirectSuccessHandler handler = new AuthRedirectSuccessHandler(authRedirectService);

    @Test
    void onAuthenticationSuccessRedirectsToStoredAllowedTarget() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        HttpSession session = request.getSession(true);
        session.setAttribute(AuthRedirectService.POST_AUTH_REDIRECT_SESSION_KEY, "http://localhost:4200/login");
        Authentication authentication = new UsernamePasswordAuthenticationToken("tester@example.com", "n/a");

        handler.onAuthenticationSuccess(request, response, authentication);

        assertEquals("http://localhost:4200/login", response.getRedirectedUrl());
        assertNull(session.getAttribute(AuthRedirectService.POST_AUTH_REDIRECT_SESSION_KEY));
    }

    @Test
    void onAuthenticationSuccessFallsBackWhenNoRedirectInSession() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.getSession(true);
        Authentication authentication = new UsernamePasswordAuthenticationToken("tester@example.com", "n/a");

        handler.onAuthenticationSuccess(request, response, authentication);

        assertEquals(AuthRedirectService.FALLBACK_REDIRECT, response.getRedirectedUrl());
    }

    @Test
    void onAuthenticationSuccessFallsBackForInvalidStoredRedirect() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        HttpSession session = request.getSession(true);
        session.setAttribute(AuthRedirectService.POST_AUTH_REDIRECT_SESSION_KEY, "https://other.example.com/login");
        Authentication authentication = new UsernamePasswordAuthenticationToken("tester@example.com", "n/a");

        handler.onAuthenticationSuccess(request, response, authentication);

        assertEquals(AuthRedirectService.FALLBACK_REDIRECT, response.getRedirectedUrl());
        assertNull(session.getAttribute(AuthRedirectService.POST_AUTH_REDIRECT_SESSION_KEY));
    }
}