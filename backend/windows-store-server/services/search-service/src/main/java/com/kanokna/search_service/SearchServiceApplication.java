package com.kanokna.search_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*<MODULE_CONTRACT
    id="mod.search.service"
    name="search-service"
    layer="bootstrap"
    version="0.1.0"
    BOUNDED_CONTEXT="search"
    LINKS="RequirementsAnalysis.xml#UC-SEARCH-BROWSE,RequirementsAnalysis.xml#UC-SEARCH-AUTOCOMPLETE,DevelopmentPlan.xml#Flow-Search,Technology.xml#Interfaces">
  <PURPOSE>Bootstrap for search-service following hexagonal architecture.</PURPOSE>
  <RESPONSIBILITIES>
    <Item>Start Spring Boot application context</Item>
    <Item>Enable adapters (REST) for search/autocomplete and listeners for indexing</Item>
  </RESPONSIBILITIES>
  <CONTEXT>
    <UPSTREAM>API gateway, frontend</UPSTREAM>
    <DOWNSTREAM>Elasticsearch/OpenSearch, Redis cache, Kafka consumers</DOWNSTREAM>
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
public class SearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}
