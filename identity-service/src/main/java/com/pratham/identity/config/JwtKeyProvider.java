package com.pratham.identity.config;

import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.github.cdimascio.dotenv.Dotenv;

@Component
public class JwtKeyProvider {
    private final SecretKey key;

    public JwtKeyProvider(@Value("${SECRET_KEY:}") String configuredSecret) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String secret = configuredSecret;
        if (secret == null || secret.isBlank()) {
            secret = System.getenv("SECRET_KEY");
        }
        if (secret == null || secret.isBlank()) {
            secret = dotenv.get("SECRET_KEY");
        }
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("SECRET_KEY must be configured as a Base64-encoded HS256 key");
        }
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("SECRET_KEY must be valid Base64", exception);
        }
        if (decoded.length < 32) {
            throw new IllegalStateException("SECRET_KEY must decode to at least 32 bytes for HS256");
        }
        this.key = Keys.hmacShaKeyFor(decoded);
    }

    public SecretKey getKey() {
        return key;
    }
}
