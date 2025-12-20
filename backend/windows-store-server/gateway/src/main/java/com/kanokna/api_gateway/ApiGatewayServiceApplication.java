package com.kanokna.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*<MODULE_CONTRACT
    id="mod.gateway"
    layer="bootstrap"
    boundedContext="edge"
    LINKS="backend/windows-store-server/services/order-service/docs/DevelopmentPlan.xml#Flow-Checkout,backend/windows-store-server/services/pricing-service/docs/DevelopmentPlan.xml#Flow-Quote"
  >
  <PURPOSE>Entry point for API Gateway using Spring Cloud Gateway to route external traffic to backend services with centralized filters.</PURPOSE>
  <RESPONSIBILITIES>
    - Bootstraps gateway context.
    - Loads route definitions for catalog, pricing, cart, order, account, media, search, notification services.
  </RESPONSIBILITIES>
  <CROSS_CUTTING>
    <SECURITY>OAuth2 resource server integration to be added at filters; enforces path-based exposure.</SECURITY>
    <OBSERVABILITY>Actuator enabled; structured logs via filters.</OBSERVABILITY>
  </CROSS_CUTTING>
</MODULE_CONTRACT>*/
@SpringBootApplication
public class ApiGatewayServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayServiceApplication.class, args);
	}

}
