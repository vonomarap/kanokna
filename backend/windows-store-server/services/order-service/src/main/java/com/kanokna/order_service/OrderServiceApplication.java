package com.kanokna.order_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*<MODULE_CONTRACT
    id="mod.order.service"
    name="order-service"
    layer="bootstrap"
    version="0.1.0"
    BOUNDED_CONTEXT="order"
    LINKS="RequirementsAnalysis.xml#UC-CHECKOUT-ORDER,RequirementsAnalysis.xml#UC-PAYMENT,DevelopmentPlan.xml#Flow-Checkout,DevelopmentPlan.xml#Flow-Payment,Technology.xml#Interfaces">
  <PURPOSE>Bootstrap for order-service following hexagonal architecture.</PURPOSE>
  <RESPONSIBILITIES>
    <Item>Start Spring Boot application context</Item>
    <Item>Enable adapters (REST/gRPC/webhook) for checkout, payments, lifecycle</Item>
  </RESPONSIBILITIES>
  <CONTEXT>
    <UPSTREAM>API gateway, cart-service, frontend</UPSTREAM>
    <DOWNSTREAM>PostgreSQL, Redis, Kafka (outbox), payment gateway</DOWNSTREAM>
  </CONTEXT>
  <CROSS_CUTTING>
    <OBSERVABILITY>Use OTEL tracing, Micrometer metrics, structured JSON logs with block anchors</OBSERVABILITY>
  </CROSS_CUTTING>
  <LOGGING>Keep bootstrap logs minimal; operational logs in adapters/application layers.</LOGGING>
  <TESTS>
    <Case id="TC-BOOT-001">Application context loads</Case>
  </TESTS>
</MODULE_CONTRACT>*/
@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
