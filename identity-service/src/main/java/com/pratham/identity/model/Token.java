package com.pratham.identity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tokens")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 1024)
    private String token;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType type = TokenType.BEARER;
    @Column(nullable = false)
    private boolean revoked;
    @Column(nullable = false)
    private boolean expired;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    protected Token() {
    }

    public Token(String token, User user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public boolean isRevoked() { return revoked; }
    public boolean isExpired() { return expired; }
    public User getUser() { return user; }
    public void revoke() { this.revoked = true; }
}
