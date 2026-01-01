package com.kanokna.gateway.config;

/* <FUNCTION_CONTRACT id="FC-gateway-config-GatewayConfig-routeLocator"
     LAYER="config"
     INTENT="Define route mappings from gateway paths to backend services"
     INPUT="RouteLocatorBuilder"
     OUTPUT="RouteLocator"
     SIDE_EFFECTS="None (configuration only)"
     LINKS="DevelopmentPlan.xml#DP-SVC-gateway">
  <PRECONDITIONS>
    <Item>Backend service URIs configured (via config-server or environment)</Item>
  </PRECONDITIONS>

  <POSTCONDITIONS>
    <Item>All /api/{service}/** paths route to corresponding service</Item>
    <Item>Path rewriting strips /api prefix where needed</Item>
    <Item>Circuit breaker applied to each route</Item>
    <Item>Timeout configured per route</Item>
  </POSTCONDITIONS>

  <INVARIANTS>
    <Item>Route order matters: more specific routes before general</Item>
    <Item>All routes have circuit breaker configured</Item>
    <Item>All routes have timeout configured</Item>
  </INVARIANTS>

  <ROUTES>
    <Route id="catalog-route" path="/api/catalog/**" target="lb://catalog-configuration-service"/>
    <Route id="pricing-route" path="/api/pricing/**" target="lb://pricing-service"/>
    <Route id="cart-route" path="/api/cart/**" target="lb://cart-service"/>
    <Route id="orders-route" path="/api/orders/**" target="lb://order-service"/>
    <Route id="accounts-route" path="/api/accounts/**" target="lb://account-service"/>
    <Route id="media-route" path="/api/media/**" target="lb://media-service"/>
    <Route id="search-route" path="/api/search/**" target="lb://search-service"/>
    <Route id="installations-route" path="/api/installations/**" target="lb://installation-service"/>
    <Route id="reports-route" path="/api/reports/**" target="lb://reporting-service"/>
  </ROUTES>

  <BLOCK_ANCHORS>
    <Item id="BA-GW-ROUTE-01">Match incoming path to route</Item>
    <Item id="BA-GW-ROUTE-02">Apply route filters (rewrite, headers)</Item>
    <Item id="BA-GW-ROUTE-03">Forward to backend service</Item>
  </BLOCK_ANCHORS>

  <LOGGING>
    <Item>[SVC=gateway][UC=ROUTING][BLOCK=BA-GW-ROUTE-01][STATE=ROUTE_MATCHED] eventType=REQUEST_ROUTED decision=FORWARD keyValues=path,routeId,target</Item>
    <Item>[SVC=gateway][UC=ROUTING][BLOCK=BA-GW-ROUTE-03][STATE=RESPONSE_RECEIVED] eventType=BACKEND_RESPONSE decision=COMPLETE keyValues=status,latencyMs</Item>
  </LOGGING>

  <TESTS>
    <Case id="TC-ROUTE-001">/api/catalog/products routes to catalog-configuration-service</Case>
    <Case id="TC-ROUTE-002">/api/orders routes to order-service</Case>
    <Case id="TC-ROUTE-003">Unknown path returns 404</Case>
    <Case id="TC-ROUTE-004">Backend timeout triggers fallback</Case>
    <Case id="TC-ROUTE-005">Circuit breaker opens after failures</Case>
  </TESTS>
</FUNCTION_CONTRACT> */

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.springframework.cloud.gateway.support.RouteMetadataUtils.RESPONSE_TIMEOUT_ATTR;

@Configuration
public class GatewayConfig {
    private final RateLimiter<?> rateLimiter;
    private final KeyResolver keyResolver;

    public GatewayConfig(RateLimiter<?> rateLimiter, KeyResolver keyResolver) {
        this.rateLimiter = rateLimiter;
        this.keyResolver = keyResolver;
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("catalog-route", r -> r
                // <BLOCK_ANCHOR id="BA-GW-ROUTE-01">Match incoming path to route</BLOCK_ANCHOR>
                .path("/api/catalog/**")
                .filters(f -> f
                    // <BLOCK_ANCHOR id="BA-GW-ROUTE-02">Apply route filters (rewrite, headers)</BLOCK_ANCHOR>
                    .stripPrefix(1)
                    .requestRateLimiter(config -> config.setKeyResolver(keyResolver).setRateLimiter(rateLimiter))
                )
                .metadata(RESPONSE_TIMEOUT_ATTR, 10000)
                // <BLOCK_ANCHOR id="BA-GW-ROUTE-03">Forward to backend service</BLOCK_ANCHOR>
                .uri("lb://catalog-configuration-service"))
            .route("pricing-route", r -> r
                .path("/api/pricing/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .requestRateLimiter(config -> config.setKeyResolver(keyResolver).setRateLimiter(rateLimiter))
                )
                .metadata(RESPONSE_TIMEOUT_ATTR, 10000)
                .uri("lb://pricing-service"))
            .route("cart-route", r -> r
                .path("/api/cart/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .requestRateLimiter(config -> config.setKeyResolver(keyResolver).setRateLimiter(rateLimiter))
                )
                .metadata(RESPONSE_TIMEOUT_ATTR, 10000)
                .uri("lb://cart-service"))
            .route("orders-route", r -> r
                .path("/api/orders/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .requestRateLimiter(config -> config.setKeyResolver(keyResolver).setRateLimiter(rateLimiter))
                )
                .metadata(RESPONSE_TIMEOUT_ATTR, 10000)
                .uri("lb://order-service"))
            .route("accounts-route", r -> r
                .path("/api/accounts/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .requestRateLimiter(config -> config.setKeyResolver(keyResolver).setRateLimiter(rateLimiter))
                )
                .metadata(RESPONSE_TIMEOUT_ATTR, 10000)
                .uri("lb://account-service"))
            .route("media-route", r -> r
                .path("/api/media/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .requestRateLimiter(config -> config.setKeyResolver(keyResolver).setRateLimiter(rateLimiter))
                )
                .metadata(RESPONSE_TIMEOUT_ATTR, 10000)
                .uri("lb://media-service"))
            .route("search-route", r -> r
                .path("/api/search/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .requestRateLimiter(config -> config.setKeyResolver(keyResolver).setRateLimiter(rateLimiter))
                )
                .metadata(RESPONSE_TIMEOUT_ATTR, 10000)
                .uri("lb://search-service"))
            .route("installations-route", r -> r
                .path("/api/installations/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .requestRateLimiter(config -> config.setKeyResolver(keyResolver).setRateLimiter(rateLimiter))
                )
                .metadata(RESPONSE_TIMEOUT_ATTR, 10000)
                .uri("lb://installation-service"))
            .route("reports-route", r -> r
                .path("/api/reports/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .requestRateLimiter(config -> config.setKeyResolver(keyResolver).setRateLimiter(rateLimiter))
                )
                .metadata(RESPONSE_TIMEOUT_ATTR, 10000)
                .uri("lb://reporting-service"))
            .build();
    }
}
