package com.kanokna.gateway.config;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

class SecurityConfigTest {

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        var context = new AnnotationConfigApplicationContext(TestConfig.class);
        webTestClient = WebTestClient
            .bindToApplicationContext(context)
            .apply(springSecurity())
            .configureClient()
            .build();
    }

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
        webTestClient.mutateWith(mockJwt())
            .get()
            .uri("/api/orders/test")
            .exchange()
            .expectStatus().isOk();
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

    @Configuration
    @EnableWebFlux
    @EnableWebFluxSecurity
    static class TestConfig {

        @Bean
        RouterFunction<ServerResponse> testRoutes() {
            return RouterFunctions.route()
                .GET("/api/orders/test", request -> ServerResponse.ok().bodyValue("ok"))
                .GET("/api/catalog/products", request -> ServerResponse.ok().bodyValue("ok"))
                .GET("/api/reports/summary", request -> ServerResponse.ok().bodyValue("ok"))
                .build();
        }

        @Bean
        SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
            return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                    .pathMatchers("/api/catalog/**").permitAll()
                    .pathMatchers("/api/reports/**").hasRole("ADMIN")
                    .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}))
                .build();
        }

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
