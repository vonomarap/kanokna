package com.kanokna.media_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*<MODULE_CONTRACT
    id="mod.media.service"
    name="media-service"
    layer="bootstrap"
    version="0.1.0"
    BOUNDED_CONTEXT="media"
    LINKS="RequirementsAnalysis.xml#UC-MEDIA-UPLOAD,RequirementsAnalysis.xml#UC-MEDIA-RESOLVE,DevelopmentPlan.xml#Flow-Upload,DevelopmentPlan.xml#Flow-Resolve,Technology.xml#Interfaces">
  <PURPOSE>Bootstrap for media-service following hexagonal architecture.</PURPOSE>
  <RESPONSIBILITIES>
    <Item>Start Spring Boot application context</Item>
    <Item>Enable adapters (REST) for upload/link/resolve</Item>
  </RESPONSIBILITIES>
  <CONTEXT>
    <UPSTREAM>API gateway, catalog-admin UI, internal services needing media URLs</UPSTREAM>
    <DOWNSTREAM>PostgreSQL, object storage (S3-compatible), Kafka (outbox)</DOWNSTREAM>
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
public class MediaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediaServiceApplication.class, args);
    }
}
