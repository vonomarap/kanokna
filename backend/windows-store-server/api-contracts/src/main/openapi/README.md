# Kanokna OpenAPI Contracts

REST API specifications for the Kanokna Windows & Doors e-commerce platform.

## API Specifications

| API | File | Description |
|-----|------|-------------|
| Cart API | [cart-api.yaml](cart-api.yaml) | Shopping cart management, promo codes, checkout |
| Catalog API | [catalog-api.yaml](catalog-api.yaml) | Product templates, configuration validation |
| Pricing API | [pricing-api.yaml](pricing-api.yaml) | Price quotes, promo code validation |
| Search API | [search-api.yaml](search-api.yaml) | Full-text search, faceted filtering, autocomplete |
| Account API | [account-api.yaml](account-api.yaml) | User profiles, addresses, order history |

## Architecture

```
┌─────────────────┐
│    Frontend     │
│  (React/Next.js)│
└────────┬────────┘
         │ REST/JSON
         ▼
┌─────────────────┐
│   API Gateway   │ ◄── OpenAPI contracts (this directory)
│   (Spring MVC)  │
└────────┬────────┘
         │ gRPC
         ▼
┌─────────────────┐
│   Microservices │
│ (gRPC/Protobuf) │
└─────────────────┘
```

## Usage

### Generate TypeScript Client

```bash
npx @openapitools/openapi-generator-cli generate \
  -i cart-api.yaml \
  -g typescript-fetch \
  -o ../../../frontend/src/api/cart
```

### Validate Specifications

```bash
npx @redocly/cli lint *.yaml
```

### Generate Documentation

```bash
npx @redocly/cli build-docs cart-api.yaml -o docs/cart-api.html
```

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.1.0 | 2026-01-10 | Cart API: Added ClearCart, RefreshPrices, extended CartItem fields |
| 1.0.0 | 2026-01-09 | Initial OpenAPI specifications |

## GRACE Traceability

These OpenAPI contracts are derived from gRPC proto definitions and aligned with GRACE artifacts:

- **Handoff**: `Handoff-20260109-01-W1-T6-CartService`
- **Proto Sources**: `api-contracts/src/main/proto/kanokna/`
- **Function Contracts**: `docs/grace/contracts/FC-*.xml`

## Authentication

### Bearer Token (JWT)
```yaml
Authorization: Bearer <jwt-token>
```
Used for authenticated users. Token obtained from authentication service.

### Session ID (Anonymous)
```yaml
X-Session-ID: <uuid>
```
Used for anonymous cart operations. Created client-side.
