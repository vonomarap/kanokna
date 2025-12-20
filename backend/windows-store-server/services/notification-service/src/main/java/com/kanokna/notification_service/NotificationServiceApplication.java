package com.kanokna.notification_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*<MODULE_CONTRACT
    id="mod.notification.service"
    name="notification-service"
    layer="bootstrap"
    version="0.1.0"
    BOUNDED_CONTEXT="notification"
    LINKS="RequirementsAnalysis.xml#UC-NOTIFY-EVENT,DevelopmentPlan.xml#Flow-Event-Driven,Technology.xml#Interfaces">
  <PURPOSE>Bootstrap for notification-service following hexagonal architecture.</PURPOSE>
  <RESPONSIBILITIES>
    <Item>Start Spring Boot application context</Item>
    <Item>Enable adapters (REST, Kafka listeners) for templates, preferences, and send</Item>
  </RESPONSIBILITIES>
  <CONTEXT>
    <UPSTREAM>order-service, cart-service, admin UI</UPSTREAM>
    <DOWNSTREAM>PostgreSQL, Redis, Kafka (inbox/outbox), email/SMS/push providers</DOWNSTREAM>
  </CONTEXT>
  <CROSS_CUTTING>
    <OBSERVABILITY>Use OTEL tracing, Micrometer metrics, structured JSON logs with block anchors</OBSERVABILITY>
  </CROSS_CUTTING>
  <LOGGING>Bootstrap logs minimal; operational logs in adapters/application layers.</LOGGING>
  <TESTS>
    <Case id="TC-BOOT-001">Application context loads</Case>
  </TESTS>
</MODULE_CONTRACT>*/
@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
