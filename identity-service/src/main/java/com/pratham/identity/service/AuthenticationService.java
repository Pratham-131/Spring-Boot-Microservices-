package com.pratham.identity.service;

import com.pratham.identity.model.Role;
import com.pratham.identity.model.Token;
import com.pratham.identity.model.User;
import com.pratham.identity.repository.TokenRepository;
import com.pratham.identity.repository.UserRepository;
import com.pratham.identity.web.AuthenticationRequest;
import com.pratham.identity.web.AuthenticationResponse;
import com.pratham.identity.web.RegisterRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository, TokenRepository tokenRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        User user = userRepository.save(new User(request.firstname(), request.lastname(), request.email(),
                passwordEncoder.encode(request.password()), Role.USER));
        return createAndStoreToken(user);
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return createAndStoreToken(user);
    }

    @Transactional
    public void logout(String rawToken) {
        Token token = tokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token not found"));
        token.revoke();
        tokenRepository.save(token);
    }

    private AuthenticationResponse createAndStoreToken(User user) {
        String jwt = jwtService.generateToken(user);
        tokenRepository.save(new Token(jwt, user));
        return new AuthenticationResponse(jwt);
    }
}
