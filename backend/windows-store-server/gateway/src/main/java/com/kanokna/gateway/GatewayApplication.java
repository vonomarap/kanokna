package com.kanokna.gateway;

/* <MODULE_CONTRACT id="MC-gateway-infrastructure-GatewayApplication"
     ROLE="InfrastructureService"
     SERVICE="gateway"
     LAYER="infrastructure"
     BOUNDED_CONTEXT="infrastructure"
     SPECIFICATION="NFR-SEC-AUTHENTICATION, NFR-OBS-TRACING">
  <PURPOSE>
    API Gateway serving as the single entry point for all frontend and external
    client requests. Routes requests to backend microservices, enforces authentication,
    applies rate limiting, injects correlation IDs, and handles CORS.
  </PURPOSE>

  <RESPONSIBILITIES>
    <Item>Route HTTP requests to backend services based on path prefixes</Item>
    <Item>Validate JWT tokens from OAuth2/OIDC provider (Keycloak dev/stage, Auth0 prod)</Item>
    <Item>Inject X-Correlation-ID header for distributed tracing (generate if missing)</Item>
    <Item>Apply rate limiting: 100 req/min authenticated, 20 req/min anonymous</Item>
    <Item>Configure CORS for allowed frontend origins</Item>
    <Item>Apply circuit breakers for backend service resilience</Item>
    <Item>Expose health endpoints for Kubernetes probes</Item>
  </RESPONSIBILITIES>

  <INVARIANTS>
    <Item>All requests to /api/** require valid JWT token (except explicitly excluded paths)</Item>
    <Item>Every response includes X-Correlation-ID header</Item>
    <Item>Requests exceeding rate limit receive 429 Too Many Requests</Item>
    <Item>Failed backend calls trigger circuit breaker after threshold</Item>
  </INVARIANTS>

  <CONTEXT>
    <UPSTREAM>
      <Item>Frontend SPA: web-frontend (browser)</Item>
      <Item>Mobile apps: mobile-app (native)</Item>
      <Item>External integrations (webhook callbacks from payment gateway)</Item>
    </UPSTREAM>
    <DOWNSTREAM>
      <Item>catalog-configuration-service: /api/catalog/**</Item>
      <Item>pricing-service: /api/pricing/**</Item>
      <Item>cart-service: /api/cart/**</Item>
      <Item>order-service: /api/orders/**</Item>
      <Item>account-service: /api/accounts/**</Item>
      <Item>media-service: /api/media/**</Item>
      <Item>search-service: /api/search/**</Item>
      <Item>installation-service: /api/installations/**</Item>
      <Item>reporting-service: /api/reports/**</Item>
    </DOWNSTREAM>
  </CONTEXT>

  <ARCHITECTURE>
    <TECHNOLOGY>
      <Item>Spring Cloud Gateway (reactive, WebFlux-based)</Item>
      <Item>Spring Security OAuth2 Resource Server</Item>
      <Item>Resilience4j for circuit breakers</Item>
      <Item>Redis for distributed rate limiting (optional, in-memory fallback)</Item>
    </TECHNOLOGY>
  </ARCHITECTURE>

  <PUBLIC_API>
    <Item>All /api/** routes proxied to backend services</Item>
    <Item>/actuator/health/liveness - Kubernetes liveness probe</Item>
    <Item>/actuator/health/readiness - Kubernetes readiness probe</Item>
    <Item>/actuator/prometheus - Metrics endpoint (internal network only)</Item>
  </PUBLIC_API>

  <CROSS_CUTTING>
    <SECURITY>
      <Item>JWT validation with RS256 signature verification</Item>
      <Item>Token issuer and audience claims validated</Item>
      <Item>Public paths: /api/catalog/products (GET), /api/search (GET), /actuator/health/**</Item>*/
//    <Item>Admin paths require ADMIN role: /api/reports/**, /api/*/admin/**</Item>
/*  </SECURITY>
    <RELIABILITY>
      <Item>Circuit breaker: 50% failure threshold, 10s open state, 3 half-open calls</Item>
      <Item>Timeout: 10s for all backend calls</Item>
      <Item>Retry: 3 attempts with 500ms backoff for 5xx errors (idempotent only)</Item>
    </RELIABILITY>
    <OBSERVABILITY>
      <Item>Structured JSON logs with correlationId, path, method, status, latencyMs</Item>
      <Item>Metrics: gateway.requests.total, gateway.requests.latency, gateway.circuit.state</Item>
      <Item>Traces propagated via W3C Trace Context headers</Item>
    </OBSERVABILITY>
  </CROSS_CUTTING>

  <LOGGING>
    <FORMAT>[SVC=gateway][UC=ROUTING][BLOCK=...][STATE=...] eventType=... decision=... keyValues=...</FORMAT>
    <EXAMPLES>
      <Item>[SVC=gateway][UC=ROUTING][BLOCK=BA-GW-ROUTE-01][STATE=ROUTE_MATCHED] eventType=REQUEST_ROUTED decision=FORWARD keyValues=path=/api/catalog/products,target=catalog-configuration-service</Item>
      <Item>[SVC=gateway][UC=AUTH][BLOCK=BA-GW-AUTH-01][STATE=TOKEN_VALIDATED] eventType=AUTH_CHECK decision=ALLOW keyValues=userId=uuid,roles=[CUSTOMER]</Item>
      <Item>[SVC=gateway][UC=RATE-LIMIT][BLOCK=BA-GW-RATE-01][STATE=LIMIT_CHECK] eventType=RATE_LIMIT decision=ALLOW|REJECT keyValues=clientId,remaining,limit</Item>
    </EXAMPLES>
  </LOGGING>

  <TESTS>
    <Case id="TC-GW-001">Valid JWT token allows request to protected endpoint</Case>
    <Case id="TC-GW-002">Missing JWT token on protected endpoint returns 401</Case>
    <Case id="TC-GW-003">Expired JWT token returns 401</Case>
    <Case id="TC-GW-004">Public endpoints accessible without token</Case>
    <Case id="TC-GW-005">Rate limit exceeded returns 429</Case>
    <Case id="TC-GW-006">Correlation ID generated if not present</Case>
    <Case id="TC-GW-007">Correlation ID propagated if present</Case>
    <Case id="TC-GW-008">Circuit breaker opens after failures</Case>
    <Case id="TC-GW-009">CORS preflight returns correct headers</Case>
    <Case id="TC-GW-010">Admin endpoint requires ADMIN role</Case>
  </TESTS>

  <LINKS>
    <Link ref="DevelopmentPlan.xml#DP-SVC-gateway"/>
    <Link ref="Technology.xml#TECH-spring-cloud"/>
    <Link ref="Technology.xml#TECH-spring-security"/>
    <Link ref="Technology.xml#TECH-resilience4j"/>
    <Link ref="RequirementsAnalysis.xml#NFR-SEC-AUTHENTICATION"/>
    <Link ref="RequirementsAnalysis.xml#NFR-OBS-TRACING"/>
  </LINKS>
</MODULE_CONTRACT>
*/

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;

@SpringBootApplication
@EnableWebFluxSecurity
public class GatewayApplication {
    static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
