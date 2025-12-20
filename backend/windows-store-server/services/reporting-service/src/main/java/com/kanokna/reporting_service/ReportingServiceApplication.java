package com.kanokna.reporting_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*<MODULE_CONTRACT
    id="mod.reporting.service"
    name="reporting-service"
    layer="bootstrap"
    version="0.1.0"
    BOUNDED_CONTEXT="reporting"
    LINKS="RequirementsAnalysis.xml#UC-REPORT-DASHBOARD,RequirementsAnalysis.xml#UC-REPORT-EXPORT,DevelopmentPlan.xml#Flow-Ingestion,DevelopmentPlan.xml#Flow-Dashboard-Query,Technology.xml#Interfaces">
  <PURPOSE>Bootstrap for reporting-service following hexagonal architecture.</PURPOSE>
  <RESPONSIBILITIES>
    <Item>Start Spring Boot application context</Item>
    <Item>Enable adapters (REST, Kafka listeners) for dashboards and ingestion</Item>
  </RESPONSIBILITIES>
  <CONTEXT>
    <UPSTREAM>API gateway, admin UI, internal services</UPSTREAM>
    <DOWNSTREAM>PostgreSQL (read models), Redis cache, Kafka consumers</DOWNSTREAM>
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
public class ReportingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportingServiceApplication.class, args);
    }
}
