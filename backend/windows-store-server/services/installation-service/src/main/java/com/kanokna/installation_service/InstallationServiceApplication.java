package com.kanokna.installation_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*<MODULE_CONTRACT
    id="mod.installation.service"
    name="installation-service"
    layer="bootstrap"
    version="0.1.0"
    BOUNDED_CONTEXT="installation"
    LINKS="RequirementsAnalysis.xml#UC-INSTALL-BOOK,DevelopmentPlan.xml#Flow-Schedule,Technology.xml#Interfaces">
  <PURPOSE>Bootstrap for installation-service following hexagonal architecture.</PURPOSE>
  <RESPONSIBILITIES>
    <Item>Start Spring Boot application context</Item>
    <Item>Enable adapters (REST) for slot search/booking and status updates</Item>
  </RESPONSIBILITIES>
  <CONTEXT>
    <UPSTREAM>API gateway, order-service, frontend</UPSTREAM>
    <DOWNSTREAM>PostgreSQL, Redis, Kafka (inbox/outbox)</DOWNSTREAM>
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
public class InstallationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstallationServiceApplication.class, args);
    }
}
