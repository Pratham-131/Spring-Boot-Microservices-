package com.pratham.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "management.tracing.enabled=false"
})
class GatewayRoutesTest {
    @DynamicPropertySource
    static void secret(DynamicPropertyRegistry registry) {
        registry.add("SECRET_KEY", () -> "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=");
    }

    @Autowired
    private RouteLocator routeLocator;

    @Test
    void configuresBothLoadBalancedServiceRoutes() {
        List<Route> routes = routeLocator.getRoutes().collectList().block();
        assertThat(routes).extracting(Route::getId)
                .contains("identity-service", "demo-controller");
    }
}
