package com.kanokna.api_gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Value("${gateway.security.enabled:true}")
    private boolean securityEnabled;
    @Value("${gateway.auth.issuer-uri:}")
    private String issuerUri;
    @Value("${gateway.auth.jwk-set-uri:}")
    private String jwkSetUri;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        if (!securityEnabled) {
            http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll());
            return http.build();
        }
        if (issuerUri.isBlank() && jwkSetUri.isBlank()) {
            throw new IllegalStateException("gateway.security.enabled=true requires gateway.auth.issuer-uri or gateway.auth.jwk-set-uri");
        }
        http.csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/**").permitAll()
                .anyExchange().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
                if (!jwkSetUri.isBlank()) {
                    jwt.jwkSetUri(jwkSetUri);
                } else if (!issuerUri.isBlank()) {
                    jwt.jwtDecoder(ReactiveJwtDecoders.fromIssuerLocation(issuerUri));
                }
            }));
        return http.build();
    }
}
