package com.kanokna.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.kanokna.gateway.StubJwtDecoderConfig;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

/**
 * Tests the REAL {@link SecurityConfig} bean to ensure production security
 * rules are correctly enforced, including public paths, admin paths, and
 * authenticated paths.
 */
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
    void searchPublicPathWithoutTokenSucceeds() {
        webTestClient.get()
                .uri("/api/search/query")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void actuatorHealthWithoutTokenSucceeds() {
        webTestClient.get()
                .uri("/actuator/health/liveness")
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

    @Test
    void catalogAdminPathRequiresAdminRole() {
        webTestClient.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                .get()
                .uri("/api/catalog/admin/settings")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void pricingAdminPathRequiresAdminRole() {
        webTestClient.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                .get()
                .uri("/api/pricing/admin/markup")
                .exchange()
                .expectStatus().isForbidden();
    }

    /**
     * Uses the REAL SecurityConfig bean instead of a simplified copy, plus stub
     * routes for all paths under test.
     */
    @Configuration
    @EnableWebFlux
    @EnableWebFluxSecurity
    @Import({SecurityConfig.class, StubJwtDecoderConfig.class})
    static class TestConfig {

        @Bean
        RouterFunction<ServerResponse> testRoutes() {
            return RouterFunctions.route()
                    .GET("/api/orders/test", request -> ServerResponse.ok().bodyValue("ok"))
                    .GET("/api/catalog/products", request -> ServerResponse.ok().bodyValue("ok"))
                    .GET("/api/catalog/products/**", request -> ServerResponse.ok().bodyValue("ok"))
                    .GET("/api/catalog/admin/**", request -> ServerResponse.ok().bodyValue("ok"))
                    .GET("/api/search/**", request -> ServerResponse.ok().bodyValue("ok"))
                    .GET("/api/media/public/**", request -> ServerResponse.ok().bodyValue("ok"))
                    .GET("/api/reports/**", request -> ServerResponse.ok().bodyValue("ok"))
                    .GET("/api/pricing/admin/**", request -> ServerResponse.ok().bodyValue("ok"))
                    .GET("/api/accounts/admin/**", request -> ServerResponse.ok().bodyValue("ok"))
                    .GET("/actuator/health/**", request -> ServerResponse.ok().bodyValue("ok"))
                    .GET("/actuator/info", request -> ServerResponse.ok().bodyValue("ok"))
                    .build();
        }
    }
}
