package com.kanokna.gateway;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.cloud.config.enabled=false"
})
@AutoConfigureWebTestClient
@org.springframework.context.annotation.Import(CorsIntegrationTest.StubJwtDecoderConfig.class)
class CorsIntegrationTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void corsPreflightReturnsHeaders() {
        webTestClient.options()
            .uri("/api/catalog/products")
            .header("Origin", "http://localhost:3000")
            .header("Access-Control-Request-Method", "GET")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Access-Control-Allow-Origin", "http://localhost:3000")
            .expectHeader().valueEquals("Access-Control-Allow-Credentials", "true");
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
