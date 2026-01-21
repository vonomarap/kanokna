package com.kanokna.gateway.route;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.cloud.config.enabled=false"
})
class RouteIT {
    @Autowired
    private RouteLocator routeLocator;

    @Autowired
    private Environment environment;

    @Test
    void routesContainExpectedMappings() {
        List<Route> routes = routeLocator.getRoutes().collectList().block();
        assertThat(routes).isNotNull();
        assertThat(routes.stream().map(Route::getId))
            .contains(
                "catalog-route",
                "pricing-route",
                "cart-route",
                "orders-route",
                "accounts-route",
                "media-route",
                "search-route",
                "installations-route",
                "reports-route"
            );

        Route catalog = routes.stream().filter(route -> route.getId().equals("catalog-route")).findFirst().orElseThrow();
        Route orders = routes.stream().filter(route -> route.getId().equals("orders-route")).findFirst().orElseThrow();

        assertThat(matchesPath(catalog, "/api/catalog/products")).isTrue();
        assertThat(matchesPath(orders, "/api/orders/123")).isTrue();
    }

    @Test
    void unknownPathHasNoRoute() {
        List<Route> routes = routeLocator.getRoutes().collectList().block();
        boolean matched = routes.stream().anyMatch(route -> matchesPath(route, "/api/unknown"));
        assertThat(matched).isFalse();
    }

    @Test
    void circuitBreakerSettingsConfigured() {
        assertThat(environment.getProperty("resilience4j.circuitbreaker.configs.default.failureRateThreshold"))
            .isEqualTo("50");
        assertThat(environment.getProperty("resilience4j.circuitbreaker.configs.default.waitDurationInOpenState"))
            .isEqualTo("10s");
    }

    @Test
    void fallbackEndpointReturnsServiceUnavailable() {
        FallbackController controller = new FallbackController();
        StepVerifier.create(controller.fallback(MockServerHttpRequest.get("/fallback").build()))
            .assertNext(response -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE))
            .verifyComplete();
    }

    private boolean matchesPath(Route route, String path) {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get(path).build());
        return Boolean.TRUE.equals(Mono.from(route.getPredicate().apply(exchange)).block());
    }

    @TestConfiguration
    static class StubJwtDecoderConfig {
        @Bean
        ReactiveJwtDecoder reactiveJwtDecoder() {
            return token -> Mono.just(Jwt.withTokenValue(token)
                .header("alg", "none")
                .claim("sub", "test-user")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build());
        }
    }
}
