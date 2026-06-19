package com.example.worldcuppredictor.domain.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

/**
 * Represents an authenticated application user, created or updated on first Google OAuth2 login.
 *
 * <p>The primary identity key is {@code googleSubject} (the Google account's stable sub claim).
 * The email is also unique but may change across logins, so {@code googleSubject} is used
 * for all internal lookups.
 */
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Stable Google account identifier (OIDC sub claim). Used as the primary lookup key. */
    @Column(unique = true, nullable = false)
    private String googleSubject;

    /** User's Google account email address. */
    @Column(unique = true, nullable = false)
    private String email;

    /** Display name from the Google profile (may be null for accounts without a name set). */
    private String fullName;

    /** URL of the user's Google profile picture. May be null. */
    private String pictureUrl;

    /** Application role controlling access level. Defaults to {@link Role#USER}. */
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    /** Timestamp when this user record was first created. */
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /** Timestamp of the user's most recent successful login. Updated on every OAuth2 callback. */
    private OffsetDateTime lastLoginAt;

    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGoogleSubject() {
        return googleSubject;
    }

    public void setGoogleSubject(String googleSubject) {
        this.googleSubject = googleSubject;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(OffsetDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}
