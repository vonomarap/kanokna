package com.kanokna.pricing_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*<MODULE_CONTRACT
    id="mod.pricing.service"
    name="pricing-service"
    layer="bootstrap"
    version="0.1.0"
    BOUNDED_CONTEXT="pricing"
    LINKS="RequirementsAnalysis.xml#UC-PRICE-PREVIEW,DevelopmentPlan.xml#Service-pricing-service,DevelopmentPlan.xml#Flow-Quote,Technology.xml#Interfaces">
  <PURPOSE>Bootstrap for pricing-service following hexagonal architecture.</PURPOSE>
  <RESPONSIBILITIES>
    <Item>Start Spring Boot application context</Item>
    <Item>Enable future adapters (REST/gRPC) for quote and admin endpoints</Item>
  </RESPONSIBILITIES>
  <CONTEXT>
    <UPSTREAM>API gateway, cart-service, order-service, frontend</UPSTREAM>
    <DOWNSTREAM>PostgreSQL, Redis, Kafka (outbox/inbox), optional FX provider</DOWNSTREAM>
  </CONTEXT>
  <CROSS_CUTTING>
    <OBSERVABILITY>Use OTEL tracing, Micrometer metrics, structured JSON logs with block anchors</OBSERVABILITY>
  </CROSS_CUTTING>
  <LOGGING>Keep bootstrap logs minimal; functional logs placed in adapters/application layers.</LOGGING>
  <TESTS>
    <Case id="TC-BOOT-001">Application context loads</Case>
  </TESTS>
</MODULE_CONTRACT>*/
@SpringBootApplication
public class PricingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PricingServiceApplication.class, args);
    }
}
