package com.kanokna.cart.adapters.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * OAuth2 resource server configuration.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF protection is intentionally disabled per DEC-SEC-CSRF-STATELESS:
            // - All APIs use stateless JWT bearer token authentication (no cookies)
            // - No browser-based form submissions or cookie-based sessions
            // - CSRF attacks require cookie-based auth which is not present
            // See: Technology.xml#DEC-SEC-CSRF-STATELESS
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/v1/cart/**").permitAll()
                .anyRequest().authenticated()
            );
            // OAuth2 resource server config to be enabled in production.
            // .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
