package com.kanokna.catalog_configuration_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*<MODULE_CONTRACT
    id="mod.catalog.configuration-service"
    layer="application"
    boundedContext="Catalog"
    SPECIFICATION="RA-CONFIGURATOR.Configure,RA-PRICING.Quote"
    LINKS="RequirementsAnalysis.xml#CONFIGURATOR.Configure,RequirementsAnalysis.xml#PRICING.Quote,Technology.xml#BACKEND,DevelopmentPlan.xml#APIs">
  <PURPOSE>
    Serve catalog configuration workflows by validating product variants, enforcing sizing/material rules, and preparing bill of materials for pricing and fulfillment flows.
  </PURPOSE>
  <RESPONSIBILITIES>
    - Maintain configuration domain model (products, variants, configuration rules, BOM templates) using shared-kernel primitives.
    - Validate configurations against dimension/material/hinge/glass compatibility rules and emit validation events.
    - Resolve bill of materials for a configuration to support pricing and downstream fulfillment.
    - Publish domain events (product activation, configuration validated, BOM resolved) for orchestrators and pricing services.
  </RESPONSIBILITIES>
  <CONTEXT>
    <UPSTREAM>
      - API Gateway/BFF invokes validation and BOM resolution endpoints; pricing and quote services consume validation outputs.
    </UPSTREAM>
    <DOWNSTREAM>
      - Shared kernel for IDs, money, localization, measurements.
      - Messaging outbox/integration layer (future) for domain events.
    </DOWNSTREAM>
  </CONTEXT>
  <ARCHITECTURE>
    <PATTERNS>
      - Hexagonal: application services orchestrate domain rules and emit DomainEvents; adapters handle transport and persistence.
      - Domain stays framework-free; contracts encoded via Module/Function contracts.
    </PATTERNS>
    <TECHNOLOGY>
      - Spring Boot 4.x base; Domain model in plain Java; future adapters for REST/GraphQL/Kafka.
    </TECHNOLOGY>
  </ARCHITECTURE>
  <PUBLIC_API>
    - Validate configuration for product variant: input configuration -> ValidationResult + ConfigurationValidatedEvent.
    - Resolve BOM for configuration: input configuration -> BillOfMaterials + BomResolvedEvent.
    - Product lifecycle: activate/retire products and emit ProductActivatedEvent.
  </PUBLIC_API>
  <DOMAIN_INVARIANTS>
    - Products transition only between DRAFT->ACTIVE->RETIRED; active flag derived from status.
    - Configurations must carry dimensions and hinge/opening orientation; signatureKey is deterministic over all fields.
    - ValidationResult combines errors deterministically; BOM merge sums quantities per component SKU.
  </DOMAIN_INVARIANTS>
  <CROSS_CUTTING>
    <SECURITY>
      - No secrets in domain logs; PII handled by upstream layers.
    </SECURITY>
    <RELIABILITY>
      - Validation must be deterministic and side-effect free; BOM resolution is pure and repeatable.
    </RELIABILITY>
    <OBSERVABILITY>
      - Domain emits events for state transitions; adapters enrich with trace/correlation IDs.
    </OBSERVABILITY>
  </CROSS_CUTTING>
  <LOGGING>
    - Domain layer avoids direct logging; adapters emit structured logs on validation and BOM resolution outcomes.
  </LOGGING>
  <TESTING_STRATEGY>
    - Happy: valid dimension/material/hinge configs pass; BOM merges templates correctly.
    - Negative: out-of-range dimensions, disallowed finishes, hinge side not allowed, incompatible extras produce errors.
    - State: DRAFT cannot be re-retired; ACTIVE->RETIRED irreversible; isActive reflects status.
  </TESTING_STRATEGY>
</MODULE_CONTRACT>
*/
@SpringBootApplication
public class CatalogConfigurationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatalogConfigurationServiceApplication.class, args);
	}

}
