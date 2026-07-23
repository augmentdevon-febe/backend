package com.example.worldcuppredictor.api.controller;

import com.example.worldcuppredictor.domain.entity.User;
import com.example.worldcuppredictor.domain.repository.UserRepository;
import com.example.worldcuppredictor.infrastructure.security.AuthRedirectService;
import com.example.worldcuppredictor.infrastructure.security.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final AuthRedirectService authRedirectService;
    private final AuthService authService;

    public AuthController(UserRepository userRepository,
                          AuthRedirectService authRedirectService,
                          AuthService authService) {
        this.userRepository = userRepository;
        this.authRedirectService = authRedirectService;
        this.authService = authService;
    }

    @GetMapping("/login")
    public void login(@RequestParam(name = "error", required = false) String error,
                      @RequestParam(name = "redirect_uri", required = false) String redirectUri,
                      @RequestParam(name = "redirectUrl", required = false) String redirectUrl,
                      @RequestParam(name = "returnUrl", required = false) String returnUrl,
                      HttpServletRequest request,
                      HttpServletResponse response) throws IOException {
        if (error != null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "OAuth login failed");
            return;
        }

        String redirectTarget = authRedirectService.resolveFromParams(redirectUri, redirectUrl, returnUrl);
        authRedirectService.storeRedirectInSession(request.getSession(true), redirectTarget);
        response.sendRedirect("/oauth2/authorization/google");
    }

    @GetMapping("/switch-account")
    public void switchAccount(@RequestParam(name = "redirect_uri", required = false) String redirectUri,
                              @RequestParam(name = "redirectUrl", required = false) String redirectUrl,
                              @RequestParam(name = "returnUrl", required = false) String returnUrl,
                              HttpServletRequest request,
                              HttpServletResponse response,
                              Authentication authentication) throws IOException {
        String redirectTarget = authRedirectService.resolveFromParams(redirectUri, redirectUrl, returnUrl);

        authService.logout(request, response, authentication);
        authRedirectService.storeRedirectInSession(request.getSession(true), redirectTarget);

        response.sendRedirect("/oauth2/authorization/google");
    }

    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> session(@AuthenticationPrincipal OidcUser principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("authenticated", false));
        }

        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "email", principal.getEmail(),
                "name", principal.getFullName(),
                "subject", principal.getSubject()
        ));
    }

    @GetMapping("/me")
    public User me(@AuthenticationPrincipal OidcUser principal) {
        if (principal == null) return null;
        return userRepository.findByGoogleSubject(principal.getSubject()).orElseGet(() -> {
            var u = new User();
            u.setEmail(principal.getEmail());
            u.setFullName(principal.getFullName());
            u.setGoogleSubject(principal.getSubject());
            return u;
        });
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request,
                                                      HttpServletResponse response,
                                                      Authentication authentication) {
        authService.logout(request, response, authentication);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logged out successfully"
        ));
    }

    @GetMapping("/logout")
    public ResponseEntity<Map<String, Object>> logoutForBrowser(HttpServletRequest request,
                                                                HttpServletResponse response,
                                                                Authentication authentication) {
        return logout(request, response, authentication);
    }

    @PostMapping("/test-session")
    public ResponseEntity<Map<String, String>> createTestSession(HttpSession session) {
        // Create or retrieve a test user
        String testSubject = "test-subject-" + System.currentTimeMillis();
        String testEmail = "test@example.com";
        String testName = "Test User";

        User user = userRepository.findByEmail(testEmail)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setGoogleSubject(testSubject);
                    newUser.setEmail(testEmail);
                    newUser.setFullName(testName);
                    newUser.setCreatedAt(OffsetDateTime.now());
                    newUser.setLastLoginAt(OffsetDateTime.now());
                    return userRepository.save(newUser);
                });

        // Create an OidcUser principal
        Map<String, Object> claims = Map.of(
                "sub", user.getGoogleSubject(),
                "email", user.getEmail(),
                "name", user.getFullName()
        );
        org.springframework.security.oauth2.core.oidc.OidcIdToken idToken =
                new org.springframework.security.oauth2.core.oidc.OidcIdToken(
                        "token_value", java.time.Instant.now(), java.time.Instant.now().plusSeconds(3600), claims);
        OidcUser oidcUser = new org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser(
                java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")),
                idToken);

        // Create authentication and store in session
        Authentication authentication = new org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken(
                oidcUser, oidcUser.getAuthorities(), "google");
        org.springframework.security.core.context.SecurityContext securityContext =
                org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        return ResponseEntity.ok(Map.of(
                "sessionId", session.getId(),
                "message", "Test session created successfully"
        ));
    }
}

