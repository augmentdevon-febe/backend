package com.example.worldcuppredictor.api.controller;

import com.example.worldcuppredictor.api.dto.request.PredictionRequest;
import com.example.worldcuppredictor.api.dto.response.PredictionDto;
import com.example.worldcuppredictor.domain.entity.Prediction;
import com.example.worldcuppredictor.domain.entity.User;
import com.example.worldcuppredictor.domain.repository.UserRepository;
import com.example.worldcuppredictor.domain.service.PredictionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {
    private final PredictionService predictionService;
    private final UserRepository userRepository;

    public PredictionController(PredictionService predictionService, UserRepository userRepository) {
        this.predictionService = predictionService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<PredictionDto> create(@AuthenticationPrincipal OidcUser principal,
                                                @Valid @RequestBody PredictionRequest req) throws Exception {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByGoogleSubject(principal.getSubject())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        PredictionDto dto = predictionService.createPrediction(user, req);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public Page<Prediction> list(@AuthenticationPrincipal OidcUser principal,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        if (principal == null) {
            throw new IllegalArgumentException("Authentication required");
        }
        User user = userRepository.findByGoogleSubject(principal.getSubject())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        Pageable p = PageRequest.of(page, size);
        return predictionService.listForUser(user, p);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prediction> get(@AuthenticationPrincipal OidcUser principal, @PathVariable Long id) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByGoogleSubject(principal.getSubject())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        return predictionService.findByIdForUser(id, user)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
