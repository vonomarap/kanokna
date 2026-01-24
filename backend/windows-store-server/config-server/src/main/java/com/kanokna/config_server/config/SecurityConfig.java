package com.kanokna.config_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for Config Server.
 *
 * <p>Configures authentication for config endpoints and actuator endpoints
 * with different security requirements.</p>
 *
 * <pre>
 * Security Zones:
 * ─────────────────────────────────────────────────────────────────────────────
 * 1. Health probes (permitAll):
 *    - /actuator/health
 *    - /actuator/health/liveness
 *    - /actuator/health/readiness
 *
 * 2. All other endpoints (authenticated):
 *    - /actuator/** (requires authentication)
 *    - /{application}/{profile}
 *    - /{application}/{profile}/{label}
 *    - /encrypt, /decrypt
 *
 * Links:
 *   - DevelopmentPlan.xml#DP-SVC-config-server
 *   - RequirementsAnalysis.xml#NFR-SEC-AUTHENTICATION
 * ─────────────────────────────────────────────────────────────────────────────
 * </pre>
 *
 * @author GRACE-CODER
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Single security filter chain for all endpoints.
     * Health probes are accessible without authentication for Kubernetes.
     * All other endpoints require basic authentication.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Health probes must be accessible without authentication for K8s
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**"
                        ).permitAll()
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .csrf(csrf -> csrf.disable()); // Disable CSRF for config API

        return http.build();
    }
}
