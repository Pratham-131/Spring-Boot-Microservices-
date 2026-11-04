package com.pratham.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.pratham.identity.config.JwtKeyProvider;
import com.pratham.identity.model.Role;
import com.pratham.identity.model.User;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private JwtService jwtService;
    private JwtKeyProvider keyProvider;

    @BeforeEach
    void setUp() {
        String secret = Base64.getEncoder().encodeToString(new byte[32]);
        keyProvider = mock(JwtKeyProvider.class);
        when(keyProvider.getKey()).thenReturn(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                Base64.getDecoder().decode(secret)));
        jwtService = new JwtService(keyProvider);
    }

    @Test
    void generatesTokenWithSubjectRoleAndTwentyFourHourExpiry() {
        String token = jwtService.generateToken(new User("A", "B", "a@example.com", "hash", Role.USER));
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractSubject(token)).isEqualTo("a@example.com");
        var claims = Jwts.parser().verifyWith(keyProvider.getKey()).build().parseSignedClaims(token).getPayload();
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    void validatesSignedToken() {
        String token = jwtService.generateToken(new User("A", "B", "b@example.com", "hash", Role.USER));
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void rejectsExpiredToken() {
        String token = Jwts.builder()
                .subject("old@example.com")
                .expiration(Date.from(Instant.now().minusSeconds(1)))
                .signWith(keyProvider.getKey())
                .compact();
        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

}
