# API Contracts

Contract-first API definitions (Protobuf/gRPC) for the Windows & Doors E-Commerce platform.

<!-- <MODULE_CONTRACT id="MC-api-contracts-proto"
     ROLE="ContractLibrary"
     SERVICE="api-contracts"
     LAYER="contracts"
     BOUNDED_CONTEXT="contracts"
     SPECIFICATION="DEC-INTER-SERVICE-COMM, DEC-EVENT-SERIALIZATION">
  <PURPOSE>
    Contract-first API specifications defining all inter-service gRPC APIs
    and Kafka domain event schemas. Generated stubs used by service adapters.
  </PURPOSE>

  <RESPONSIBILITIES>
    <Item>Define gRPC service interfaces (CatalogConfiguration, Pricing, Cart, Account, Media)</Item>
    <Item>Define Protobuf message schemas for domain events</Item>
    <Item>Define common types (Money, Dimensions, Address, Pagination)</Item>
    <Item>Generate Java stubs via protobuf-maven-plugin</Item>
    <Item>Enforce backward compatibility via buf lint</Item>
  </RESPONSIBILITIES>

  <INVARIANTS>
    <Item>All proto files follow Google style guide</Item>
    <Item>Field numbers never reused after deletion (use reserved)</Item>
    <Item>New fields are optional (no required in proto3)</Item>
    <Item>Package naming: kanokna.{domain}.v{version}</Item>
    <Item>Generated stubs confined to adapters (ArchUnit enforced)</Item>
  </INVARIANTS>

  <CONSTRAINTS>
    <Item>MUST NOT contain business logic</Item>
    <Item>MUST NOT depend on Spring Boot runtime</Item>
    <Item>MUST NOT depend on JPA/Hibernate</Item>
    <Item>Domain layer MUST NOT import generated types</Item>
  </CONSTRAINTS>

  <GRPC_SERVICES>
    <Service name="CatalogConfigurationService" package="kanokna.catalog.v1">
      Configuration validation, product queries
    </Service>
    <Service name="PricingService" package="kanokna.pricing.v1">
      Quote calculation
    </Service>
    <Service name="CartService" package="kanokna.cart.v1">
      Cart operations, snapshot for checkout
    </Service>
    <Service name="AccountService" package="kanokna.account.v1">
      User profile queries
    </Service>
    <Service name="MediaService" package="kanokna.media.v1">
      Media metadata queries
    </Service>
  </GRPC_SERVICES>

  <EVENT_SCHEMAS>
    <Topic name="catalog.product.published">ProductTemplatePublishedEvent</Topic>
    <Topic name="order.created">OrderCreatedEvent</Topic>
    <Topic name="order.status.changed">OrderStatusChangedEvent</Topic>
    <Topic name="payment.captured">PaymentCapturedEvent</Topic>
    <Topic name="installation.scheduled">InstallationScheduledEvent</Topic>
    <Topic name="installation.completed">InstallationCompletedEvent</Topic>
  </EVENT_SCHEMAS>

  <TESTS>
    <Case id="TC-PROTO-001">mvn compile generates gRPC stubs without errors</Case>
    <Case id="TC-PROTO-002">buf lint passes for all proto files</Case>
    <Case id="TC-PROTO-003">buf breaking detects removed fields</Case>
    <Case id="TC-PROTO-004">All messages have field documentation</Case>
  </TESTS>

  <LINKS>
    <Link ref="DevelopmentPlan.xml#DP-SVC-api-contracts"/>
    <Link ref="DevelopmentPlan.xml#ContractEvolutionPolicy"/>
    <Link ref="Technology.xml#TECH-protobuf"/>
    <Link ref="Technology.xml#TECH-grpc"/>
  </LINKS>
</MODULE_CONTRACT> -->

## Directory Structure

```
src/main/proto/kanokna/
├── common/v1/          # Shared types
│   ├── money.proto     # Money and Currency
│   ├── dimensions.proto # Product dimensions
│   ├── address.proto   # Postal address
│   └── common.proto    # Pagination, EventMetadata, LocalizedText
├── catalog/v1/         # Catalog service
│   ├── catalog_configuration_service.proto  # gRPC service
│   └── catalog_events.proto                 # Domain events
├── pricing/v1/         # Pricing service
│   ├── pricing_service.proto  # gRPC service
│   └── pricing_events.proto   # Domain events
├── cart/v1/            # Cart service
│   └── cart_service.proto     # gRPC service
├── account/v1/         # Account service
│   └── account_service.proto  # gRPC service
├── media/v1/           # Media service
│   └── media_service.proto    # gRPC service
├── order/v1/           # Order events
│   └── order_events.proto     # Domain events
└── installation/v1/    # Installation events
    └── installation_events.proto  # Domain events
```

## Building

```bash
# Generate Java stubs
mvn compile

# Clean and rebuild
mvn clean compile
```

## gRPC Services

| Service | Package | Description |
|---------|---------|-------------|
| CatalogConfigurationService | kanokna.catalog.v1 | Product configuration validation |
| PricingService | kanokna.pricing.v1 | Quote calculation |
| CartService | kanokna.cart.v1 | Shopping cart operations |
| AccountService | kanokna.account.v1 | User profile management |
| MediaService | kanokna.media.v1 | Media asset management |

## Domain Events

| Topic | Event | Description |
|-------|-------|-------------|
| catalog.product.published | ProductTemplatePublishedEvent | Product is available |
| order.created | OrderCreatedEvent | New order placed |
| order.status.changed | OrderStatusChangedEvent | Order status transition |
| payment.captured | PaymentCapturedEvent | Payment successful |
| installation.scheduled | InstallationScheduledEvent | Installation booked |
| installation.completed | InstallationCompletedEvent | Installation done |

## Contract Evolution Policy

- **Add optional fields only** - never remove or rename existing fields
- **Use `reserved`** for removed field numbers to prevent reuse
- **Backward compatible** - old clients must work with new services
- **Run buf lint** before merging proto changes
