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
        String picture = (String) oidcUser.getAttributes().get("picture");

        userRepository.findByGoogleSubject(subject).or(() -> userRepository.findByEmail(email))
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
