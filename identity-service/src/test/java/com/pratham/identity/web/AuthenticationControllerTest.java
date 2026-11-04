package com.pratham.identity.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
class AuthenticationControllerTest {
    @DynamicPropertySource
    static void secret(DynamicPropertyRegistry registry) {
        registry.add("SECRET_KEY", () -> java.util.Base64.getEncoder().encodeToString(new byte[32]));
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerNewUserReturnsToken() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstname":"Controller","lastname":"Register","email":"new-user@example.com","password":"password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void duplicateEmailReturnsConflictJson() throws Exception {
        String request = """
                {"firstname":"Controller","lastname":"Duplicate","email":"duplicate@example.com","password":"password"}
                """;
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already registered"));
    }

    @Test
    void authenticateValidCredentialsReturnsToken() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstname":"Controller","lastname":"Login","email":"valid-login@example.com","password":"password"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/authenticate").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"valid-login@example.com","password":"password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void authenticateBadPasswordReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstname":"Controller","lastname":"Bad Password","email":"bad-password@example.com","password":"password"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/authenticate").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"bad-password@example.com","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void logoutWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidRegistrationInputReturnsBadRequestJson() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstname":"","lastname":"Invalid","email":"not-an-email","password":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

}
