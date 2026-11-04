package com.pratham.gateway.security;

import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final JwtKeyProvider keyProvider;

    public JwtService(JwtKeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(keyProvider.getKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException exception) {
            return false;
        }
    }
}
