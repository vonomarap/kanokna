package com.kanokna.gateway.config;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@SpringBootTest(properties = {
    "spring.cloud.config.enabled=false"
})
@AutoConfigureWebTestClient
@Import({SecurityConfig.class, SecurityConfigTest.TestJwtDecoderConfig.class, SecurityConfigTest.TestController.class})
class SecurityConfigTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void publicPathWithoutTokenSucceeds() {
        webTestClient.get()
            .uri("/api/catalog/products")
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    void protectedPathWithoutTokenReturns401() {
        webTestClient.get()
            .uri("/api/orders/test")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void protectedPathWithValidTokenSucceeds() {
        webTestClient.get()
            .uri("/api/orders/test")
            .headers(headers -> headers.setBearerAuth("valid-user"))
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    void expiredTokenReturns401() {
        webTestClient.get()
            .uri("/api/orders/test")
            .headers(headers -> headers.setBearerAuth("expired"))
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void adminPathWithCustomerRoleReturns403() {
        webTestClient.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
            .get()
            .uri("/api/reports/summary")
            .exchange()
            .expectStatus().isForbidden();
    }

    @Test
    void adminPathWithAdminRoleSucceeds() {
        webTestClient.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
            .get()
            .uri("/api/reports/summary")
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    void invalidSignatureReturns401() {
        webTestClient.get()
            .uri("/api/orders/test")
            .headers(headers -> headers.setBearerAuth("invalid"))
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @RestController
    static class TestController {
        @GetMapping("/api/orders/test")
        public Mono<String> protectedEndpoint() {
            return Mono.just("ok");
        }

        @GetMapping("/api/catalog/products")
        public Mono<String> publicCatalog() {
            return Mono.just("ok");
        }

        @GetMapping("/api/reports/summary")
        public Mono<String> adminEndpoint() {
            return Mono.just("ok");
        }
    }

    @TestConfiguration
    static class TestJwtDecoderConfig {
        @Bean
        ReactiveJwtDecoder reactiveJwtDecoder() {
            return token -> {
                if ("expired".equals(token)) {
                    return Mono.error(new JwtException("expired"));
                }
                if ("invalid".equals(token)) {
                    return Mono.error(new JwtException("invalid"));
                }
                return Mono.just(Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("sub", "user")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(300))
                    .build());
            };
        }
    }
}
