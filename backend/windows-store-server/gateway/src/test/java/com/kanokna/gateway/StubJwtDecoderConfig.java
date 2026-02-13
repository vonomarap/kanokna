package com.kanokna.gateway;

import java.time.Instant;

import reactor.core.publisher.Mono;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

/**
 * Shared test stub for JWT decoding. Used across multiple test classes to avoid
 * duplicating the same TestConfiguration.
 *
 * <p>
 * Accepts any token except "expired" and "invalid" which simulate error cases.
 */
@TestConfiguration
public class StubJwtDecoderConfig {

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
                    .claim("sub", "test-user")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(300))
                    .build());
        };
    }
}
