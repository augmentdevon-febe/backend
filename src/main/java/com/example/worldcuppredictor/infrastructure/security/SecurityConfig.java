package com.example.worldcuppredictor.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Spring Security configuration for the application.
 *
 * <p>Security rules:
 * <ul>
 *   <li>Public endpoints: {@code /api/health}, {@code /api/auth/login},
 *       {@code /api/auth/switch-account}, OAuth2 redirect URIs, H2 console,
 *       and Swagger/OpenAPI docs.</li>
 *   <li>All other requests require an authenticated session.</li>
 * </ul>
 *
 * <p>CSRF is disabled for {@code /api/**} and {@code /h2-console/**} to support REST clients
 * and the embedded database console.
 *
 * <p>After a successful Google OAuth2 login the user is redirected to
 * {@code /api/auth/session}. Authentication failures redirect to
 * {@code /api/auth/login?error=true}. Unauthenticated API requests receive HTTP 401 instead
 * of the default redirect to the login page.
 *
 * <p>OAuth2 authorization requests include {@code prompt=select_account} so users can choose
 * a different Google account when starting authentication.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String OAUTH2_AUTHORIZATION_BASE_URI = "/oauth2/authorization";

    private final CustomOidcUserService oidcUserService;
    private final AuthRedirectSuccessHandler authRedirectSuccessHandler;
    private final ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;
    private final ApiAccessDeniedHandler apiAccessDeniedHandler;

    public SecurityConfig(
            CustomOidcUserService oidcUserService,
            AuthRedirectSuccessHandler authRedirectSuccessHandler,
            ApiAuthenticationEntryPoint apiAuthenticationEntryPoint,
            ApiAccessDeniedHandler apiAccessDeniedHandler
    ) {
        this.oidcUserService = oidcUserService;
        this.authRedirectSuccessHandler = authRedirectSuccessHandler;
        this.apiAuthenticationEntryPoint = apiAuthenticationEntryPoint;
        this.apiAccessDeniedHandler = apiAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ClientRegistrationRepository clientRegistrationRepository
    ) throws Exception {
        SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler("/api/auth/login?error=true");

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health", "/api/auth/login", "/api/auth/session", "/api/auth/logout", "/api/auth/switch-account", "/api/auth/test-session", "/oauth2/**", "/login/oauth2/**", "/error", "/h2-console/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated()
                )
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        new AntPathRequestMatcher("/h2-console/**"),
                        new AntPathRequestMatcher("/api/**")
                ))
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                apiAuthenticationEntryPoint,
                                new AntPathRequestMatcher("/api/**")
                        )
                        .defaultAccessDeniedHandlerFor(
                                apiAccessDeniedHandler,
                                new AntPathRequestMatcher("/api/**")
                        )
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .oauth2Login(oauth -> oauth
                        .loginPage("/api/auth/login")
                        .authorizationEndpoint(authz -> authz
                                .baseUri(OAUTH2_AUTHORIZATION_BASE_URI)
                                .authorizationRequestResolver(authorizationRequestResolver(clientRegistrationRepository))
                        )
                        .redirectionEndpoint(redir -> redir.baseUri("/login/oauth2/code/*"))
                        .successHandler(authRedirectSuccessHandler)
                        .failureHandler(failureHandler)
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService))
                )
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll());

        return http.build();
    }

    private OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {
        DefaultOAuth2AuthorizationRequestResolver delegate =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, OAUTH2_AUTHORIZATION_BASE_URI);

        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                return customize(delegate.resolve(request));
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                return customize(delegate.resolve(request, clientRegistrationId));
            }

            private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest request) {
                if (request == null) {
                    return null;
                }

                Map<String, Object> additionalParameters = new LinkedHashMap<>(request.getAdditionalParameters());
                additionalParameters.put("prompt", "select_account");

                return OAuth2AuthorizationRequest.from(request)
                        .additionalParameters(additionalParameters)
                        .build();
            }
        };
    }
}
