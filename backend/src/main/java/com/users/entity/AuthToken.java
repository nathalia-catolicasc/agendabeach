package com.users.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_tokens")
public class AuthToken {
    @Id
    @Column(name = "token_hash", length = 64)
    private String tokenHash;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    protected AuthToken() {}

    public AuthToken(String hash, User user, LocalDateTime expiresAt) {
        this.tokenHash = hash;
        this.user = user;
        this.expiresAt = expiresAt;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
