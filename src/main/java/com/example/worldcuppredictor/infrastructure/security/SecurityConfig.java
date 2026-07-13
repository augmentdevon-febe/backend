package com.example.worldcuppredictor.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security configuration for the application.
 *
 * <p>Security rules:
 * <ul>
 *   <li>Public endpoints: {@code /api/health}, {@code /api/auth/login}, OAuth2 redirect URIs,
 *       H2 console, and Swagger/OpenAPI docs.</li>
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
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomOidcUserService oidcUserService;
        private final ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;
        private final ApiAccessDeniedHandler apiAccessDeniedHandler;

        public SecurityConfig(
                        CustomOidcUserService oidcUserService,
                        ApiAuthenticationEntryPoint apiAuthenticationEntryPoint,
                        ApiAccessDeniedHandler apiAccessDeniedHandler
        ) {
        this.oidcUserService = oidcUserService;
                this.apiAuthenticationEntryPoint = apiAuthenticationEntryPoint;
                this.apiAccessDeniedHandler = apiAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setDefaultTargetUrl("/api/auth/session");
        successHandler.setAlwaysUseDefaultTargetUrl(true);

        SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler("/api/auth/login?error=true");

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health", "/api/auth/login", "/api/auth/test-session", "/oauth2/**", "/login/oauth2/**", "/error", "/h2-console/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated()
                )
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
                        .authorizationEndpoint(authz -> authz.baseUri("/oauth2/authorization"))
                        .redirectionEndpoint(redir -> redir.baseUri("/login/oauth2/code/*"))
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService))
                )
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll());

        return http.build();
    }
}
