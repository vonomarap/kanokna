# Catalog Configuration Service

Product catalog and configuration validation service for the Windows & Doors E-Commerce platform.

## Overview

This service manages product templates (windows, doors), configuration options, validation rules, and BOM resolution.

### Ports
- **HTTP**: 8081
- **gRPC**: 9081

### Database
- **Engine**: PostgreSQL 16
- **Schema**: catalog_configuration

### Key Responsibilities
- Manage product templates with CRUD operations
- Define option groups (materials, glazing, colors, accessories)
- Validate customer configurations against business rules
- Resolve bill of materials (BOM) for configurations
- Publish catalog versions with snapshots

## Architecture

Hexagonal architecture (ports and adapters):

```
domain/
├── model/          # Pure domain entities and value objects
├── service/        # Domain services (validation, BOM resolution)
├── event/          # Domain events
└── exception/      # Domain exceptions

application/
├── port/
│   ├── in/        # Inbound ports (use cases)
│   └── out/       # Outbound ports (repositories, clients)
├── service/       # Application services (orchestration)
└── dto/           # Data transfer objects

adapters/
├── in/
│   ├── web/       # REST controllers
│   └── grpc/      # gRPC service
├── out/
│   ├── persistence/  # JPA repositories
│   ├── grpc/         # gRPC clients
│   └── kafka/        # Event publisher
└── config/        # Configuration classes
```

## Domain Model

### Aggregates
- **ProductTemplate**: Product definitions with dimensions and options
- **ConfigurationRuleSet**: Business rules for validating configurations
- **BomTemplate**: Bill of materials templates
- **CatalogVersion**: Versioned catalog snapshots

### Key Value Objects
- **Configuration**: Customer's dimension + option selections
- **ValidationResult**: Validation outcome with errors
- **DimensionConstraints**: Min/max width/height (50-400 cm)
- **ResolvedBom**: Concrete SKUs and quantities

## Semantic Contracts

This service implements **5 FUNCTION_CONTRACTs** and **1 MODULE_CONTRACT**:

1. **MC-catalog-configuration-service-domain-ConfigurationValidation**
2. **FC-...-validateConfiguration** (8 test cases: TC-VAL-001 to TC-VAL-008)
3. **FC-...-resolveBom** (3 test cases: TC-BOM-001 to TC-BOM-003)
4. **FC-...-createProductTemplate** (4 test cases: TC-ADMIN-CREATE-*)
5. **FC-...-updateProductTemplate** (4 test cases: TC-ADMIN-UPDATE-*)
6. **FC-...-publishCatalogVersion** (6 test cases: TC-ADMIN-PUBLISH-*)

## Running the Service

### Prerequisites
- Java 25
- PostgreSQL 16
- Maven 3.9+

### Local Development

```bash
# Start PostgreSQL
docker run -d \
  --name catalog-postgres \
  -e POSTGRES_DB=catalog_configuration \
  -e POSTGRES_USER=kanokna \
  -e POSTGRES_PASSWORD=kanokna_dev \
  -p 5432:5432 \
  postgres:16

# Run service
mvn spring-boot:run -pl catalog-configuration-service

# Or with dev profile
mvn spring-boot:run -pl catalog-configuration-service -Dspring-boot.run.profiles=dev
```

### Docker

```bash
# Build image
docker build -t catalog-configuration-service -f catalog-configuration-service/Dockerfile .

# Run container
docker run -p 8081:8081 -p 9081:9081 \
  -e DB_USERNAME=kanokna \
  -e DB_PASSWORD=kanokna_dev \
  catalog-configuration-service
```

## Testing

```bash
# Run all tests
mvn test -pl catalog-configuration-service

# Run only unit tests
mvn test -pl catalog-configuration-service -Dtest=*Test

# Run only integration tests
mvn verify -pl catalog-configuration-service -Dtest=*IT

# Run ArchUnit tests
mvn test -pl catalog-configuration-service -Dtest=ArchitectureTest
```

## API Endpoints

### Public Catalog (no auth required)
- `GET /api/catalog/products` - List product templates
- `GET /api/catalog/products/{id}` - Get product template details
- `POST /api/catalog/configure/validate` - Validate configuration

### Admin Operations (requires CATALOG_ADMIN or ADMIN role)
- `POST /api/admin/catalog/products` - Create product template
- `PUT /api/admin/catalog/products/{id}` - Update product template
- `POST /api/admin/catalog/publish` - Publish catalog version

### Health & Metrics
- `GET /actuator/health` - Health check
- `GET /actuator/prometheus` - Prometheus metrics

## Configuration

Key configuration properties in `application.yml`:

```yaml
server.port: 8081

spring:
  grpc:
    server:
      port: 9081
  datasource:
    url: jdbc:postgresql://localhost:5432/catalog_configuration
  jpa:
    hibernate.ddl-auto: validate
  flyway:
    schemas: catalog_configuration
```

## Block Anchors

The service uses **22 block anchors** for traceability:
- BA-CFG-VAL-01 to BA-CFG-VAL-99: Configuration validation
- BA-BOM-RESOLVE-01 to BA-BOM-RESOLVE-03: BOM resolution
- BA-CAT-CREATE-01 to BA-CAT-CREATE-04: Create product template
- BA-CAT-UPDATE-01 to BA-CAT-UPDATE-04: Update product template
- BA-CAT-PUBLISH-01 to BA-CAT-PUBLISH-06: Publish catalog version

All logs reference these anchors for end-to-end traceability.

## License

Copyright © 2025 Kanokna
