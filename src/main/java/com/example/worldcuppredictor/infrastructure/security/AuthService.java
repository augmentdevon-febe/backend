package com.example.worldcuppredictor.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final List<String> AUTH_COOKIE_NAMES = List.of("JSESSIONID", "SESSION");

    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        HttpSession session = request.getSession(false);
        boolean hadSession = session != null;

        if (session != null) {
            try {
                session.invalidate();
            } catch (IllegalStateException ignored) {
                // Session may already be invalidated by concurrent requests.
            }
        }

        SecurityContextHolder.clearContext();

        for (String cookieName : AUTH_COOKIE_NAMES) {
            response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie(cookieName));
        }

        log.info(
                "auth.logout user={} hadSession={} path={}",
                resolveUserIdentifier(authentication),
                hadSession,
                request.getRequestURI()
        );
    }

    private static String expiredCookie(String cookieName) {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(Duration.ZERO)
                .build()
                .toString();
    }

    private static String resolveUserIdentifier(Authentication authentication) {
        if (authentication == null) {
            return "anonymous";
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OidcUser oidcUser) {
            if (oidcUser.getEmail() != null && !oidcUser.getEmail().isBlank()) {
                return oidcUser.getEmail();
            }
            return oidcUser.getSubject();
        }

        return authentication.getName();
    }
}