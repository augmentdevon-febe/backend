package com.example.worldcuppredictor.domain.repository;

import com.example.worldcuppredictor.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link User} persistence.
 *
 * <p>Provides standard CRUD operations plus lookups by Google subject and email,
 * both used during the OAuth2 login flow to match or create user records.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Finds a user by their stable Google account identifier (OIDC {@code sub} claim).
     * This is the primary lookup during login.
     */
    Optional<User> findByGoogleSubject(String googleSubject);

    /**
     * Finds a user by email address. Used as a fallback when the Google subject is not
     * yet linked, e.g. for users created before the OAuth2 integration was added.
     */
    Optional<User> findByEmail(String email);
}
