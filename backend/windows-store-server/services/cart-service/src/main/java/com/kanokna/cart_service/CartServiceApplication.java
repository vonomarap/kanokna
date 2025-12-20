package com.kanokna.cart_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*<MODULE_CONTRACT
    id="mod.cart.service"
    name="cart-service"
    layer="bootstrap"
    version="0.1.0"
    BOUNDED_CONTEXT="cart"
    LINKS="RequirementsAnalysis.xml#UC-CART-ADD-ITEM,DevelopmentPlan.xml#Service-cart-service,DevelopmentPlan.xml#Flow-Cart-Add-Update,Technology.xml#Interfaces">
  <PURPOSE>Bootstrap for cart-service following hexagonal architecture.</PURPOSE>
  <RESPONSIBILITIES>
    <Item>Start Spring Boot application context</Item>
    <Item>Enable adapters (REST/gRPC) for cart commands/queries</Item>
  </RESPONSIBILITIES>
  <CONTEXT>
    <UPSTREAM>API gateway, frontend, order-service</UPSTREAM>
    <DOWNSTREAM>PostgreSQL, Redis, Kafka (outbox), pricing-service, catalog-configuration-service</DOWNSTREAM>
  </CONTEXT>
  <CROSS_CUTTING>
    <OBSERVABILITY>Use OTEL tracing, Micrometer metrics, structured JSON logs with block anchors</OBSERVABILITY>
  </CROSS_CUTTING>
  <LOGGING>Bootstrap logs minimal; operational logs reside in adapters/application layers.</LOGGING>
  <TESTS>
    <Case id="TC-BOOT-001">Application context loads</Case>
  </TESTS>
</MODULE_CONTRACT>*/
@SpringBootApplication
public class CartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartServiceApplication.class, args);
    }
}
