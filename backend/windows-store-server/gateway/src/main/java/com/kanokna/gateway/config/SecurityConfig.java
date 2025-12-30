/* <FUNCTION_CONTRACT id="FC-gateway-config-SecurityConfig-securityWebFilterChain"
     LAYER="config"
     INTENT="Configure OAuth2 Resource Server security for JWT token validation"
     INPUT="ServerHttpSecurity"
     OUTPUT="SecurityWebFilterChain"
     SIDE_EFFECTS="None (configuration only)"
     LINKS="RequirementsAnalysis.xml#NFR-SEC-AUTHENTICATION;Technology.xml#TECH-spring-security">
  <PRECONDITIONS>
    <Item>JWKS endpoint is configured (spring.security.oauth2.resourceserver.jwt.jwk-set-uri)</Item>
    <Item>Token issuer is configured (spring.security.oauth2.resourceserver.jwt.issuer-uri)</Item>
  </PRECONDITIONS>

  <POSTCONDITIONS>
    <Item>All /api/** paths require authentication except explicitly permitted</Item>
    <Item>Public paths accessible without token</Item>
    <Item>Admin paths require ADMIN role in JWT</Item>
    <Item>CSRF disabled for stateless API</Item>
  </POSTCONDITIONS>

  <INVARIANTS>
    <Item>JWT signature verified against JWKS</Item>
    <Item>Token expiration enforced</Item>
    <Item>Token issuer claim validated</Item>
  </INVARIANTS>

  <ERROR_HANDLING>
    <Item type="BUSINESS" code="401">Invalid or missing JWT token</Item>
    <Item type="BUSINESS" code="403">Valid token but insufficient role</Item>
  </ERROR_HANDLING>

  <BLOCK_ANCHORS>
    <Item id="BA-GW-AUTH-01">Validate JWT token</Item>
    <Item id="BA-GW-AUTH-02">Extract roles from JWT claims</Item>
    <Item id="BA-GW-AUTH-03">Authorize request based on path and roles</Item>
  </BLOCK_ANCHORS>

  <LOGGING>
    <Item>[SVC=gateway][UC=AUTH][BLOCK=BA-GW-AUTH-01][STATE=TOKEN_VALIDATED] eventType=AUTH_CHECK decision=ALLOW|DENY keyValues=userId,roles,path</Item>
    <Item>[SVC=gateway][UC=AUTH][BLOCK=BA-GW-AUTH-03][STATE=AUTHORIZATION] eventType=AUTHZ_CHECK decision=ALLOW|DENY keyValues=requiredRole,userRoles,path</Item>
  </LOGGING>

  <PUBLIC_PATHS>
    <Path>/api/catalog/products</Path>
    <Path>/api/catalog/products/**</Path>
    <Path>/api/search/**</Path>
    <Path>/api/media/public/**</Path>
    <Path>/actuator/health/**</Path>
    <Path>/actuator/info</Path>
  </PUBLIC_PATHS>

  <ADMIN_PATHS>
    <Path>/api/reports/**</Path>
    <Path>/api/catalog/admin/**</Path>
    <Path>/api/pricing/admin/**</Path>
    <Path>/api/accounts/admin/**</Path>
  </ADMIN_PATHS>

  <TESTS>
    <Case id="TC-SEC-001">Request to public path without token succeeds</Case>
    <Case id="TC-SEC-002">Request to protected path without token returns 401</Case>
    <Case id="TC-SEC-003">Request with valid token to protected path succeeds</Case>
    <Case id="TC-SEC-004">Request with expired token returns 401</Case>
    <Case id="TC-SEC-005">Request to admin path with CUSTOMER role returns 403</Case>
    <Case id="TC-SEC-006">Request to admin path with ADMIN role succeeds</Case>
    <Case id="TC-SEC-007">Request with invalid signature returns 401</Case>
  </TESTS>
</FUNCTION_CONTRACT> */
package com.kanokna.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            // <BLOCK_ANCHOR id="BA-GW-AUTH-01">Validate JWT token</BLOCK_ANCHOR>
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                // <BLOCK_ANCHOR id="BA-GW-AUTH-03">Authorize request based on path and roles</BLOCK_ANCHOR>
                .pathMatchers(
                    "/actuator/health/**",
                    "/actuator/info",
                    "/api/catalog/products",
                    "/api/catalog/products/**",
                    "/api/search/**",
                    "/api/media/public/**"
                ).permitAll()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers(
                    "/api/reports/**",
                    "/api/catalog/admin/**",
                    "/api/pricing/admin/**",
                    "/api/accounts/admin/**"
                ).hasRole("ADMIN")
                .pathMatchers("/api/**").authenticated()
                .anyExchange().permitAll()
            )
            .oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt)
            .build();
    }
}
