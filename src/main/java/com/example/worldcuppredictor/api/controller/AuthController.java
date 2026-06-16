package com.example.worldcuppredictor.api.controller;

import com.example.worldcuppredictor.domain.entity.User;
import com.example.worldcuppredictor.domain.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}
