package com.kanokna.account_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*<MODULE_CONTRACT
    id="mod.account.service"
    name="account-service"
    layer="bootstrap"
    version="0.1.0"
    BOUNDED_CONTEXT="account"
    LINKS="RequirementsAnalysis.xml#UC-ACC-PROFILE,RequirementsAnalysis.xml#UC-ACC-ADDRESS,DevelopmentPlan.xml#Flow-Profile,DevelopmentPlan.xml#Flow-Address,Technology.xml#Interfaces">
  <PURPOSE>Bootstrap for account-service following hexagonal architecture.</PURPOSE>
  <RESPONSIBILITIES>
    <Item>Start Spring Boot application context</Item>
    <Item>Enable adapters (REST/gRPC) for profile, address, saved configurations, and role management</Item>
  </RESPONSIBILITIES>
  <CONTEXT>
    <UPSTREAM>API gateway, frontend, internal services needing lookups</UPSTREAM>
    <DOWNSTREAM>PostgreSQL, Redis, Kafka (outbox), external IdP</DOWNSTREAM>
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
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
