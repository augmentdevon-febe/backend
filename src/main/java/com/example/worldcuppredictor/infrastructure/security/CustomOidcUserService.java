package com.example.worldcuppredictor.infrastructure.security;

import com.example.worldcuppredictor.domain.entity.User;
import com.example.worldcuppredictor.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Custom OIDC user service that upserts the local {@link User} record on every Google login.
 *
 * <p>Spring Security calls {@link #loadUser} after a successful Google OAuth2 callback.
 * This implementation:
 * <ol>
 *   <li>Delegates to the standard {@link OidcUserService} to validate the OIDC token and
 *       retrieve the {@code UserInfo} endpoint claims.</li>
 *   <li>Looks up the user by Google subject ({@code sub} claim) or email. If found, the
 *       profile fields and {@code lastLoginAt} timestamp are updated.</li>
 *   <li>If no existing record matches, a new {@link User} is created with the {@code USER}
 *       role and persisted to the database.</li>
 *   <li>Returns the original {@link OidcUser} so Spring Security continues with standard
 *       authority and session handling.</li>
 * </ol>
 */
@Service
public class CustomOidcUserService extends OidcUserService {
    private final UserRepository userRepository;
    private final Logger log = LoggerFactory.getLogger(CustomOidcUserService.class);

    public CustomOidcUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);

        String subject = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String picture = oidcUser.getAttributes().get("picture") instanceof String ? (String) oidcUser.getAttributes().get("picture") : null;

        if (subject == null || subject.isBlank()) {
            log.error("OIDC login failed because the user subject is missing");
            throw new IllegalArgumentException("OIDC user subject is required");
        }
        if (email == null || email.isBlank()) {
            log.error("OIDC login failed because the user email is missing for subject {}", subject);
            throw new IllegalArgumentException("OIDC user email is required");
        }

        userRepository.findByGoogleSubject(subject)
                .or(() -> userRepository.findByEmail(email))
                .map(u -> {
                    u.setFullName(name);
                    u.setPictureUrl(picture);
                    u.setLastLoginAt(OffsetDateTime.now());
                    log.debug("Updating existing user {}", email);
                    return userRepository.save(u);
                })
                .orElseGet(() -> {
                    User u = new User();
                    u.setGoogleSubject(subject);
                    u.setEmail(email);
                    u.setFullName(name);
                    u.setPictureUrl(picture);
                    u.setCreatedAt(OffsetDateTime.now());
                    u.setLastLoginAt(OffsetDateTime.now());
                    log.info("Creating new user {}", email);
                    return userRepository.save(u);
                });

        return oidcUser;
    }
}
