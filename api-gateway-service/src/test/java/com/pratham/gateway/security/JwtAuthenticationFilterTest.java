package com.pratham.gateway.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pratham.gateway.security.JwtKeyProvider;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class JwtAuthenticationFilterTest {
    private JwtAuthenticationFilter filter;
    private JwtService jwtService;
    private javax.crypto.SecretKey signingKey;

    @BeforeEach
    void setUp() {
        JwtKeyProvider keyProvider = mock(JwtKeyProvider.class);
        signingKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(new byte[32]);
        when(keyProvider.getKey()).thenReturn(signingKey);
        jwtService = new JwtService(keyProvider);
        filter = new JwtAuthenticationFilter(jwtService);
    }

    @Test
    void allowsValidBearerToken() {
        String token = Jwts.builder().subject("user@example.com")
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .signWith(signingKey).compact();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                protectedRequest().header("Authorization", "Bearer " + token));
        AtomicBoolean forwarded = new AtomicBoolean();
        StepVerifier.create(filter.filter(exchange, chain(forwarded))).verifyComplete();
        assertThat(forwarded).isTrue();
    }

    @Test
    void rejectsMissingToken() {
        MockServerWebExchange exchange = protectedExchange();
        AtomicBoolean forwarded = new AtomicBoolean();
        StepVerifier.create(filter.filter(exchange, chain(forwarded))).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(forwarded).isFalse();
    }

    @Test
    void rejectsInvalidToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(protectedRequest().header(
                "Authorization", "Bearer not-a-jwt"));
        StepVerifier.create(filter.filter(exchange, chain(new AtomicBoolean()))).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void rejectsExpiredToken() {
        String expired = Jwts.builder().subject("user@example.com")
                .expiration(Date.from(Instant.now().minusSeconds(1)))
                .signWith(signingKey).compact();
        MockServerWebExchange exchange = MockServerWebExchange.from(protectedRequest().header(
                "Authorization", "Bearer " + expired));
        StepVerifier.create(filter.filter(exchange, chain(new AtomicBoolean()))).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private MockServerWebExchange protectedExchange() {
        return MockServerWebExchange.from(protectedRequest());
    }

    private MockServerHttpRequest.BaseBuilder<?> protectedRequest() {
        return MockServerHttpRequest.get("/demo-controller/api/test/demo-controller/greet");
    }

    private GatewayFilterChain chain(AtomicBoolean forwarded) {
        return exchange -> {
            forwarded.set(true);
            return Mono.empty();
        };
    }
}
