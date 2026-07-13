package com.example.worldcuppredictor.infrastructure.security;

import jakarta.servlet.http.HttpSession;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuthRedirectService {

    public static final String POST_AUTH_REDIRECT_SESSION_KEY = "POST_AUTH_REDIRECT";
    public static final String FALLBACK_REDIRECT = "http://localhost:4200/login";

    private static final List<String> ALLOWED_ORIGIN_PREFIXES = List.of(
            "http://localhost:4200",
            "https://localhost:4200"
    );

    public String resolveFromParams(String redirectUri, String redirectUrl, String returnUrl) {
        String candidate = firstNonBlank(redirectUri, redirectUrl, returnUrl);
        if (isAllowedRedirect(candidate)) {
            return candidate;
        }
        return FALLBACK_REDIRECT;
    }

    public String consumeRedirectFromSession(HttpSession session) {
        if (session == null) {
            return FALLBACK_REDIRECT;
        }
        Object value = session.getAttribute(POST_AUTH_REDIRECT_SESSION_KEY);
        session.removeAttribute(POST_AUTH_REDIRECT_SESSION_KEY);

        if (value instanceof String redirect && isAllowedRedirect(redirect)) {
            return redirect;
        }
        return FALLBACK_REDIRECT;
    }

    public void storeRedirectInSession(HttpSession session, String redirectTarget) {
        session.setAttribute(POST_AUTH_REDIRECT_SESSION_KEY, redirectTarget);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private boolean isAllowedRedirect(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return false;
        }

        URI uri;
        try {
            uri = new URI(candidate);
        } catch (URISyntaxException ex) {
            return false;
        }

        if (!uri.isAbsolute()) {
            return false;
        }

        String normalized = uri.getScheme() + "://" + uri.getHost() + resolvePortSuffix(uri);
        return ALLOWED_ORIGIN_PREFIXES.stream().anyMatch(normalized::equalsIgnoreCase);
    }

    private static String resolvePortSuffix(URI uri) {
        if (uri.getPort() < 0) {
            return "";
        }
        return ":" + uri.getPort();
    }
}