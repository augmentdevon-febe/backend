package com.example.worldcuppredictor.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class AuthRedirectSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(AuthRedirectSuccessHandler.class);

    private final AuthRedirectService authRedirectService;

    public AuthRedirectSuccessHandler(AuthRedirectService authRedirectService) {
        this.authRedirectService = authRedirectService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        String target = authRedirectService.consumeRedirectFromSession(session);
        log.info("auth.login.success user={} redirect={}", authentication.getName(), target);
        response.sendRedirect(target);
    }
}