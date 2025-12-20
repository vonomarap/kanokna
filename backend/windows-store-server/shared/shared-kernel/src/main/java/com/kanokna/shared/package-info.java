/*<MODULE_CONTRACT
    id="mod.shared.kernel"
    name="Shared Kernel"
    layer="shared-kernel"
    boundedContext="Platform"
    SPECIFICATION="RA-CONSTRAINTS.PRICE_CURRENCY,RA-ORDER.IDEMPOTENCY,Technology.xml#MoneyTime"
    LINKS="RequirementsAnalysis.xml#PRICING.CURRENCY,RequirementsAnalysis.xml#ORDER.IDEMPOTENCY,Technology.xml#MoneyTime,Technology.xml#Patterns">
  <PURPOSE>
    Provide immutable, validated primitives and domain-level abstractions (IDs, value objects, money, dimensions, localization, domain events) used across bounded contexts without leaking infrastructure concerns.
  </PURPOSE>
  <RESPONSIBILITIES>
    - Offer canonical value objects for identity, email, address, and localized text with strict validation and normalization.
    - Supply monetary primitives (Money + rounding policy) enforcing currency consistency and deterministic rounding.
    - Define measurement/value helpers (dimensions, thickness enums) and domain event contract for cross-module messaging.
    - Remain side-effect free and framework-agnostic to protect domain purity and reusability.
  </RESPONSIBILITIES>
  <CONTEXT>
    <UPSTREAM>
      - Domain models and application services in services (catalog, checkout, pricing) consume these primitives for validation and persistence safety.
    </UPSTREAM>
    <DOWNSTREAM>
      - None directly; integrates only with JDK types to avoid infrastructure coupling.
    </DOWNSTREAM>
  </CONTEXT>
  <ARCHITECTURE>
    <PATTERNS>
      - Shared-kernel value objects with immutability and validation on creation.
      - Hexagonal alignment: no Spring/JPA/web dependencies; serialization left to adapters.
    </PATTERNS>
    <TECHNOLOGY>
      - Java 25 core types; BigDecimal/Currency for money; UUID for identifiers.
      - Bean Validation messages supported via consuming layers; MapStruct/serialization handled upstream.
    </TECHNOLOGY>
  </ARCHITECTURE>
  <PUBLIC_API>
    - Id.of(value)/random(): create stable identifiers with non-blank semantics.
    - Email.of(raw): produce canonical email addresses (NFKC + Punycode).
    - Address(record): validated postal address with canonical optional line2.
    - Money.of(amount,currency[,policy]): scaled, rounded money with arithmetic enforcing currency equality.
    - LocalizedString.resolve(language): retrieve localized text with fallback order.
    - DimensionsCm(width,height): enforce min/max constraints and compute area/fitting checks.
    - DomainEvent contract: eventId + occurredAt + type() default.
  </PUBLIC_API>
  <DOMAIN_INVARIANTS>
    - All value objects are immutable once constructed and validated at creation time.
    - Money operations require matching Currency; amounts are rounded to currency default fraction digits.
    - Id values are non-null, non-blank; Email and Address are canonical and validated.
    - Localization requires at least one translation; fallback order always yields a value.
    - Dimensions respect min/max bounds and never allow non-positive values.
  </DOMAIN_INVARIANTS>
  <CROSS_CUTTING>
    <SECURITY>
      - No PII logging inside primitives; masking provided where applicable (Email.masked).
    </SECURITY>
    <RELIABILITY>
      - Pure functions with no I/O; deterministic rounding policies for reproducible calculations.
    </RELIABILITY>
    <OBSERVABILITY>
      - Primitives avoid logging to keep side-effect free; callers log at boundaries with correlation IDs.
    </OBSERVABILITY>
  </CROSS_CUTTING>
  <LOGGING>
    - Avoid logging within shared-kernel; rely on upstream layers to emit structured logs with correlation data.
  </LOGGING>
  <TESTING_STRATEGY>
    - Creation happy/negative cases for each value object (null/blank/invalid formats, bounds).
    - Money arithmetic with matching/mismatched currencies; rounding for currencies with 0/2/3 fraction digits.
    - Localization fallback resolution when requested language missing; builder prevents empty translations.
    - Dimensions area overflow prevention and fit checks; Id.random uniqueness sanity.
  </TESTING_STRATEGY>
</MODULE_CONTRACT>
*/
package com.kanokna.shared;
