# MODULE_MAP: pricing-service

**Service:** pricing-service  
**Package:** `com.kanokna.pricing`  
**Wave:** W1 (CPQ Core)  
**Task:** W1-T3

---

## Contracts

| Type | ID | File |
|------|-----|------|
| MC | MC-pricing-service-domain-PriceCalculation | [MC-pricing-service-domain-PriceCalculation.xml](file:///c:/windows-store-project/kanokna-copy/backend/windows-store-server/docs/grace/contracts/MC-pricing-service-domain-PriceCalculation.xml) |

---

## BlockAnchors

| Prefix | Count | Purpose |
|--------|-------|---------|
| BA-PRC-CALC-* | 9 | Price calculation flow |
| BA-PROMO-VAL-* | 4 | Promo code validation |
| BA-PB-CREATE-* | 4 | Price book creation |
| BA-PB-PUBLISH-* | 6 | Price book publish flow |

---

## Package Structure

```
com.kanokna.pricing
├── application/
│   └── service/         # PricingApplicationService
├── domain/
│   ├── model/           # PriceBook, Quote, PromoCode
│   ├── calculation/     # PriceCalculationEngine
│   └── event/           # PriceBookPublishedEvent
├── infrastructure/
│   ├── persistence/     # JPA repositories
│   └── cache/           # Redis quote cache
└── api/
    └── grpc/            # PricingServiceGrpc
```
