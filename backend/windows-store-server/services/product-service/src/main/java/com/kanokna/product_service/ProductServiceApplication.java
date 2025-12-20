/*<MODULE_CONTRACT
    id="mod.catalog.product-service"
    layer="application"
    boundedContext="Catalog"
    SPECIFICATION="RA-CATALOG.Browse,RA-CATALOG.Search,RA-CONFIGURATOR.Configure"
    LINKS="RequirementsAnalysis.xml#CATALOG.Browse,RequirementsAnalysis.xml#CATALOG.Search,RequirementsAnalysis.xml#CONFIGURATOR.Configure,Technology.xml#BACKEND,DevelopmentPlan.xml#APIs">
  <PURPOSE>
    Provide the catalog and configurator backend for windows and doors, enforcing physical and pricing constraints while exposing browse/detail APIs and validated inputs for pricing and installation flows.
  </PURPOSE>
  <RESPONSIBILITIES>
    - Persist and expose window/door product definitions with audited attributes, construction specifications, pricing, currency, and media metadata.
    - Enforce domain constraints: dimension ranges and step increments, hinge/divider limits, required opening direction/type, currency normalization, and discount rules.
    - Serve paginated catalog browse/search and product detail responses (REST/GraphQL) with locale and currency context for B2C/B2B consumers.
    - Prepare validated payloads for price previews, lead-time promises, and search indexing using mappers to keep the domain independent from transport concerns.
  </RESPONSIBILITIES>
  <CONTEXT>
    <UPSTREAM>
      - API Gateway/BFF calls REST and GraphQL handlers for catalog browsing, product detail retrieval, and configuration validation.
    </UPSTREAM>
    <DOWNSTREAM>
      - MySQL database via Spring Data JPA for product persistence.
      - Elasticsearch/OpenSearch for catalog search and autocomplete indexing.
      - Redis cache (optional) for product summaries and option catalogs.
    </DOWNSTREAM>
  </CONTEXT>
  <ARCHITECTURE>
    <PATTERNS>
      - Hexagonal architecture: inbound ports (REST/GraphQL) to application services to domain model to outbound ports (repositories, search, cache).
      - Domain model uses JPA entities/embeddables with Bean Validation and auditing; MapStruct for DTO mapping to maintain domain purity.
      - Idempotency-Key required for any mutating commands (future create/update endpoints) to satisfy ORDER.IDEMPOTENCY.
    </PATTERNS>
    <TECHNOLOGY>
      - Spring Boot 4.x, Spring Web, Spring for GraphQL, Spring Security OAuth2 Resource Server, Spring Data JPA, MapStruct.
      - MySQL with Flyway migrations; Redis cache optional; Elasticsearch/OpenSearch integration for search.
      - Micrometer metrics, OpenTelemetry tracing, Logback JSON logs.
    </TECHNOLOGY>
  </ARCHITECTURE>
  <PUBLIC_API>
    - GET /api/v1/catalog/products : page + filters + locale -> list of product summaries with pricing and types.
    - GET /api/v1/catalog/products/{id} : id + locale -> product detail with construction specs, options, pricing, media.
    - GraphQL configurator queries: productOptions(productId, region, locale), validateConfiguration(input), pricePreview(input).
  </PUBLIC_API>
  <DOMAIN_INVARIANTS>
    - actualPrice > 0 and discountPrice is null or <= actualPrice, all in the declared Currency enum.
    - ConstructionSpecs width/height within [50cm, 500cm] and respect 10mm step rules per family; profile and glass references are required; lamination is optional but canonical when present.
    - Door numberOfHinges between 2 and 5; doorType and openingDirection are always present.
    - Window numberOfDividers is positive and <= 10; openable/tiltable/sill flags are explicitly set (never null).
    - Made-to-order products must have positive lead time; stock items expose availability.
  </DOMAIN_INVARIANTS>
  <CROSS_CUTTING>
    <SECURITY>
      - Resource server with JWT roles: BUYER_* for browse/detail; ADMIN for authoring/import tasks.
      - Enforce locale/region scoping when returning options and pricing.
    </SECURITY>
    <RELIABILITY>
      - Read endpoints must be cache-aware and tolerant to search/cache outages (serve from primary DB as fallback).
      - Mutating commands must be idempotent using Idempotency-Key and deduplicated at adapters.
    </RELIABILITY>
    <OBSERVABILITY>
      - Capture Micrometer timers for catalog list/detail and configurator queries; emit business metrics (conversion-related fetch counts).
      - Propagate traceId/spanId through REST/GraphQL and downstream calls; record cache hit/miss and search fallback tags.
    </OBSERVABILITY>
  </CROSS_CUTTING>
  <LOGGING>
    - Structured JSON logs keyed by correlationId/traceId/spanId.
    - Belief-state logs: CATALOG_LIST_REQUESTED, CATALOG_LIST_FALLBACK_DB, PRODUCT_DETAIL_NOT_FOUND, CONFIG_VALIDATION_FAILED, PRICE_PREVIEW_PREPARED.
    - Log at WARN on validation failures or search/cache degradation; DEBUG for payload shapes (ids, filter summaries, page/size) without PII.
  </LOGGING>
  <TESTING_STRATEGY>
    - Happy paths: list products with filters; retrieve detail for window and door; validate configuration within limits; price preview with currency conversions.
    - Negative: invalid dimension step, hinge/divider bounds, unknown currency, missing required enums, not-found ids.
    - Security: unauthenticated/forbidden access to browse/detail/configurator; token without required roles.
    - Resilience: search outage falls back to DB; cache miss/hit correctness; idempotency on repeated mutating requests.
  </TESTING_STRATEGY>
</MODULE_CONTRACT>

<FUNCTION_CONTRACT
    id="listProducts"
    module="mod.catalog.product-service"
    SPECIFICATION="RA-CATALOG.Browse"
    LINKS="MODULE_CONTRACT#mod.catalog.product-service,RequirementsAnalysis.xml#CATALOG.Browse,Technology.xml#APIs">
  <ROLE_IN_MODULE>
    Handles paginated catalog browsing for windows and doors, applying filters and locale/currency context while honoring pricing and dimension constraints.
  </ROLE_IN_MODULE>
  <SIGNATURE>
    <INPUT>
      - page:int >=0 and size:int within service limit (e.g., <=100).
      - locale:string (e.g., en, de, fr, ru) and currency:Currency enum.
      - filters: familyType(Window|Door), textQuery(optional), priceRange(min,max) in currency, dimensions(widthCm,heightCm) respecting 10mm step, optionIds(optional).
      - headers: Authorization bearer token, Correlation-Id/Idempotency-Key (for future write passthrough).
    </INPUT>
    <OUTPUT>
      - Paged list containing id, title, type, pricing (actual/discount, currency), leadTime/availability, hero media, and brief construction summary.
    </OUTPUT>
    <SIDE_EFFECTS>
      - Reads from search index or DB; optional Redis cache use; structured logs and metrics emitted.
    </SIDE_EFFECTS>
  </SIGNATURE>
  <PRECONDITIONS>
    - Caller authenticated with BUYER_* or ADMIN role.
    - Pagination parameters within allowed range; filters use known enums and valid numeric bounds.
  </PRECONDITIONS>
  <POSTCONDITIONS>
    - Result size <= requested page size; totals reflect data source used.
    - All returned items respect pricing/discount invariant and dimension constraints.
    - Locale and currency fields reflect requested context or service defaults.
  </POSTCONDITIONS>
  <INVARIANTS>
    - No unpublished or invalid products are emitted.
    - Currency and numeric fields are normalized; discountPrice is null or <= actualPrice.
  </INVARIANTS>
  <ERROR_HANDLING>
    - 400 for invalid filters/pagination or unsupported locale/currency.
    - 401/403 for unauthenticated or unauthorized callers.
    - 5xx/503 when all data sources unavailable after fallback attempts.
  </ERROR_HANDLING>
  <LOGGING>
    - DEBUG: CATALOG_LIST_REQUESTED {filters,page,size,locale,currency}.
    - INFO: CATALOG_LIST_SERVED {source=db|search, count, durationMs}.
    - WARN: CATALOG_LIST_FALLBACK_DB {reason}.
  </LOGGING>
  <TEST_CASES>
    <HAPPY_PATH>
      - List windows with pagination and locale=en, currency=USD; verify pricing and dimensions present.
      - Apply familyType=Door and priceRange filter; ensure only matching doors returned.
    </HAPPY_PATH>
    <EDGE_CASES>
      - size beyond limit -> 400; invalid dimension step -> 400; empty result set returns empty page with totals=0.
      - Search service down -> fallback to DB and log fallback flag.
    </EDGE_CASES>
    <SECURITY_CASES>
      - Missing/expired token -> 401; token without BUYER/ADMIN -> 403; tampered token -> 401.
    </SECURITY_CASES>
  </TEST_CASES>
</FUNCTION_CONTRACT>

<FUNCTION_CONTRACT
    id="getProductDetail"
    module="mod.catalog.product-service"
    SPECIFICATION="RA-CATALOG.Browse"
    LINKS="MODULE_CONTRACT#mod.catalog.product-service,RequirementsAnalysis.xml#CATALOG.Browse,RequirementsAnalysis.xml#CONFIGURATOR.Configure,Technology.xml#APIs">
  <ROLE_IN_MODULE>
    Retrieves a single product (window or door) with full construction specs, options, pricing, and media, ready for configurator validation and price preview.
  </ROLE_IN_MODULE>
  <SIGNATURE>
    <INPUT>
      - productId:long >0.
      - locale:string and currency:Currency enum.
      - headers: Authorization bearer token; correlationId/trace headers.
    </INPUT>
    <OUTPUT>
      - Product detail DTO including constructionSpecs(width,height,profile,glass,lamination), type-specific attributes (hinges, openingDirection, dividers), pricing (actual/discount), currency, leadTime/availability, media, and validation constraints for configurator UI.
    </OUTPUT>
    <SIDE_EFFECTS>
      - DB read (and cache lookup if enabled); optional search index read; structured logs and metrics.
    </SIDE_EFFECTS>
  </SIGNATURE>
  <PRECONDITIONS>
    - Caller authenticated with BUYER_* or ADMIN role.
    - productId provided and well-formed; locale/currency supported.
  </PRECONDITIONS>
  <POSTCONDITIONS>
    - Returned entity respects domain invariants (pricing, dimensions, hinges/dividers) and includes audit timestamps.
    - Not-found id results in a 404 without leaking existence of unpublished products.
  </POSTCONDITIONS>
  <INVARIANTS>
    - Pricing uses declared currency; discountPrice is null or <= actualPrice.
    - ConstructionSpecs have non-null profile and glass references; lamination is optional but canonical when present.
  </INVARIANTS>
  <ERROR_HANDLING>
    - 404 when productId not found or not visible.
    - 400 for unsupported locale/currency.
    - 401/403 for unauthenticated/unauthorized callers.
    - 5xx/503 for persistence/search/cache outages after retries/fallbacks.
  </ERROR_HANDLING>
  <LOGGING>
    - DEBUG: PRODUCT_DETAIL_REQUESTED {productId, locale, currency}.
    - INFO: PRODUCT_DETAIL_SERVED {productId, source, durationMs}.
    - WARN: PRODUCT_DETAIL_NOT_FOUND {productId} or PRODUCT_DETAIL_FALLBACK_DB {reason}.
  </LOGGING>
  <TEST_CASES>
    <HAPPY_PATH>
      - Retrieve window detail with full construction specs; verify pricing and audit timestamps present.
      - Retrieve door with hinges and openingDirection populated; locale conversion applied.
    </HAPPY_PATH>
    <EDGE_CASES>
      - Unknown id -> 404; unsupported currency -> 400; search fallback triggers when index unavailable.
    </EDGE_CASES>
    <SECURITY_CASES>
      - Missing token -> 401; token without BUYER/ADMIN -> 403; token with wrong tenant/locale -> rejected per policy.
    </SECURITY_CASES>
  </TEST_CASES>
</FUNCTION_CONTRACT>
*/
package com.kanokna.product_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ProductServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductServiceApplication.class, args);
	}

}
