package com.kanokna.api_gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/* <MODULE_CONTRACT
     id="mod.gateway.routes"
     layer="configuration"
     boundedContext="edge"
     LINKS="backend/windows-store-server/backend/docs/Technology.xml#Interfaces"
   >
   <PURPOSE>Defines core HTTP routes for backend services with path-based routing and simple rewrite filters.</PURPOSE>
   <PUBLIC_API>
     - /api/v1/catalog/** -> catalog-configuration-service
     - /api/v1/pricing/** -> pricing-service
     - /api/v1/cart/** -> cart-service
     - /api/v1/orders/** -> order-service
     - /api/v1/account/** -> account-service
     - /api/v1/media/** -> media-service
     - /api/v1/search/** -> search-service
   </PUBLIC_API>
   <CROSS_CUTTING>
     <OBSERVABILITY>Per-route logging filter suggested; actuator available.</OBSERVABILITY>
     <RELIABILITY>Configure timeouts/retries via application.yml per route as needed.</RELIABILITY>
   </CROSS_CUTTING>
 </MODULE_CONTRACT> */
@Configuration
public class GatewayRoutesConfig {

    @Value("${services.catalog.uri:lb://catalog-configuration-service}")
    private String catalogUri;
    @Value("${services.pricing.uri:lb://pricing-service}")
    private String pricingUri;
    @Value("${services.cart.uri:lb://cart-service}")
    private String cartUri;
    @Value("${services.order.uri:lb://order-service}")
    private String orderUri;
    @Value("${services.account.uri:lb://account-service}")
    private String accountUri;
    @Value("${services.media.uri:lb://media-service}")
    private String mediaUri;
    @Value("${services.search.uri:lb://search-service}")
    private String searchUri;

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("catalog", r -> r.path("/api/v1/catalog/**")
                .uri(catalogUri))
            .route("pricing", r -> r.path("/api/v1/pricing/**")
                .uri(pricingUri))
            .route("cart", r -> r.path("/api/v1/cart/**")
                .uri(cartUri))
            .route("order", r -> r.path("/api/v1/orders/**")
                .uri(orderUri))
            .route("account", r -> r.path("/api/v1/account/**")
                .uri(accountUri))
            .route("media", r -> r.path("/api/v1/media/**")
                .uri(mediaUri))
            .route("search", r -> r.path("/api/v1/search/**")
                .uri(searchUri))
            .build();
    }
}
