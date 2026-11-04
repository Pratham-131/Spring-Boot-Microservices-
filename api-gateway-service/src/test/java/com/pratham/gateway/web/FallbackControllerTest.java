package com.pratham.gateway.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class FallbackControllerTest {
    private final FallbackController controller = new FallbackController();

    @Test
    void identityFallbackReturnsServiceUnavailableAndJsonError() {
        ResponseEntity<java.util.Map<String, String>> response = controller.identityFallback();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).containsEntry("error", "Identity Service unavailable. Try later.");
    }

    @Test
    void demoFallbackReturnsServiceUnavailableAndJsonError() {
        ResponseEntity<java.util.Map<String, String>> response = controller.demoFallback();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).containsEntry("error", "Demo Controller unavailable. Try later.");
    }
}
