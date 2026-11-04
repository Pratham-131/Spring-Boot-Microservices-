package com.pratham.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pratham.identity.web.AuthenticationRequest;
import com.pratham.identity.web.AuthenticationResponse;
import com.pratham.identity.web.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class AuthenticationServiceTest {
    @DynamicPropertySource
    static void secret(DynamicPropertyRegistry registry) {
        registry.add("SECRET_KEY", () -> java.util.Base64.getEncoder().encodeToString(new byte[32]));
    }

    @Autowired
    private AuthenticationService authenticationService;

    @Test
    void registersUserAndReturnsJwt() {
        AuthenticationResponse response = authenticationService.register(
                new RegisterRequest("New", "User", "register@example.com", "password"));
        assertThat(response.token()).isNotBlank();
    }

    @Test
    void authenticatesRegisteredUser() {
        authenticationService.register(new RegisterRequest("Login", "User", "login@example.com", "password"));
        AuthenticationResponse response = authenticationService.authenticate(
                new AuthenticationRequest("login@example.com", "password"));
        assertThat(response.token()).isNotBlank();
    }

    @Test
    void rejectsBadCredentials() {
        authenticationService.register(new RegisterRequest("Bad", "Password", "bad@example.com", "password"));
        assertThatThrownBy(() -> authenticationService.authenticate(
                new AuthenticationRequest("bad@example.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
