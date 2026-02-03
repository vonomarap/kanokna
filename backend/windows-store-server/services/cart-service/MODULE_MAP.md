# MODULE_MAP: cart-service

**Service:** cart-service  
**Package:** `com.kanokna.cart`  
**Wave:** W1 (CPQ Core)  
**Task:** W1-T6  
**Schema Version:** grace-markup-v2

---

## Contracts

### Function Contracts (10 total)

| ID | Intent | Location |
|----|--------|----------|
| FC-cart-getCart | Retrieve or create cart for customer/session | [FC-cart-service-functions.xml](file:///c:/windows-store-project/kanokna-copy/backend/windows-store-server/docs/grace/contracts/FC-cart-service-functions.xml) |
| FC-cart-addItem | Add configured product to cart | ^ |
| FC-cart-updateItem | Update item quantity | ^ |
| FC-cart-removeItem | Remove item from cart | ^ |
| FC-cart-applyPromoCode | Apply promo code to cart | ^ |
| FC-cart-removePromoCode | Remove applied promo code | ^ |
| FC-cart-clearCart | Clear all items from cart | ^ |
| FC-cart-refreshPrices | Refresh prices for all items | ^ |
| FC-cart-mergeCarts | Merge anonymous cart into authenticated cart | ^ |
| FC-cart-createSnapshot | Create immutable checkout snapshot | ^ |

### Module Contracts (15 total)

| ID | Layer | Location |
|----|-------|----------|
| MC-cart-service-domain | domain | [MC-cart-service-domain.xml](file:///c:/windows-store-project/kanokna-copy/backend/windows-store-server/docs/grace/contracts/MC-cart-service-domain.xml) |
| MC-cart-orchestrator | application.service | CartApplicationService.java (inline) |
| MC-cart-checkout | application.service | CartCheckoutService.java (inline) |
| MC-cart-item-validation | application.service | CartItemValidationService.java (inline) |
| MC-cart-merging | application.service | CartMergingService.java (inline) |
| MC-cart-pricing | application.service | CartPricingService.java (inline) |
| MC-cart-promo-code | application.service | CartPromoCodeService.java (inline) |
| MC-cart-domain-errors | domain.exception | CartDomainErrors.java (inline) |
| MC-cart-grpc-adapter | adapters.in.grpc | CartGrpcService.java |
| MC-cart-persistence-adapter | adapters.out.persistence | CartRepositoryAdapter.java |
| MC-cart-kafka-adapter | adapters.out.kafka | CartKafkaEventPublisher.java |
| MC-cart-redis-adapter | adapters.out.redis | RedisSessionCartStore.java |
| MC-cart-grpc-clients | adapters.out.grpc | *GrpcClient.java |
| MC-cart-properties | adapters.config | CartProperties.java (inline) |

---

## BlockAnchors (44 total)

### GetCart (5)
| ID | Purpose |
|----|---------|
| BA-CART-GET-01 | Resolve cart ID from customerId or sessionId |
| BA-CART-GET-02 | Load cart from repository |
| BA-CART-GET-03 | Revalidate item configurations (lazy) |
| BA-CART-GET-04 | Check price staleness for each item |
| BA-CART-GET-05 | Return cart response |

### AddItem (7)
| ID | Purpose |
|----|---------|
| BA-CART-ADD-01 | Resolve or create cart |
| BA-CART-ADD-02 | Validate configuration via catalog-configuration-service |
| BA-CART-ADD-03 | Get/verify price quote from pricing-service |
| BA-CART-ADD-04 | Create CartItem with configuration snapshot |
| BA-CART-ADD-05 | Compute configuration hash |
| BA-CART-ADD-06 | Recalculate cart totals |
| BA-CART-ADD-07 | Persist cart and publish event |

### UpdateItem (5)
| ID | Purpose |
|----|---------|
| BA-CART-UPDATE-01 | Load cart and find item |
| BA-CART-UPDATE-02 | Validate quantity |
| BA-CART-UPDATE-03 | Update quantity and line total |
| BA-CART-UPDATE-04 | Recalculate cart totals |
| BA-CART-UPDATE-05 | Persist and publish event |

### RemoveItem (4)
| ID | Purpose |
|----|---------|
| BA-CART-REMOVE-01 | Load cart and find item |
| BA-CART-REMOVE-02 | Remove item from cart |
| BA-CART-REMOVE-03 | Recalculate cart totals |
| BA-CART-REMOVE-04 | Persist and publish event |

### ApplyPromoCode (5)
| ID | Purpose |
|----|---------|
| BA-CART-PROMO-01 | Load cart and validate not empty |
| BA-CART-PROMO-02 | Call pricing-service.ValidatePromoCode |
| BA-CART-PROMO-03 | Apply discount to cart |
| BA-CART-PROMO-04 | Recalculate totals |
| BA-CART-PROMO-05 | Persist and publish event |

### RemovePromoCode (4)
| ID | Purpose |
|----|---------|
| BA-CART-PROMO-REMOVE-01 | Load cart |
| BA-CART-PROMO-REMOVE-02 | Remove promo code |
| BA-CART-PROMO-REMOVE-03 | Recalculate totals |
| BA-CART-PROMO-REMOVE-04 | Persist and publish event |

### ClearCart (6)
| ID | Purpose |
|----|---------|
| BA-CART-CLEAR-01 | Resolve cart from customerId or sessionId |
| BA-CART-CLEAR-02 | Validate cart exists and caller authorized |
| BA-CART-CLEAR-03 | Remove all items from cart |
| BA-CART-CLEAR-04 | Remove applied promo code |
| BA-CART-CLEAR-05 | Reset cart totals to zero |
| BA-CART-CLEAR-06 | Persist and publish CartClearedEvent |

### RefreshPrices (7)
| ID | Purpose |
|----|---------|
| BA-CART-REFRESH-01 | Load cart and validate not empty |
| BA-CART-REFRESH-02 | Call pricing-service.CalculateQuote for each item |
| BA-CART-REFRESH-03 | Update item prices and quote IDs |
| BA-CART-REFRESH-04 | Clear price_stale flags |
| BA-CART-REFRESH-05 | Recalculate cart totals |
| BA-CART-REFRESH-06 | Recalculate promo discount if applied |
| BA-CART-REFRESH-07 | Persist and publish CartPricesRefreshedEvent |

---

## Package Structure

```
com.kanokna.cart (70 files)
├── CartServiceApplication.java

├── application/                          # Application Layer (53 files)
│   ├── dto/                              # Commands, Queries, DTOs (22)
│   │   ├── AddItemCommand                ├── GetCartQuery
│   │   ├── UpdateItemCommand             ├── CartDto
│   │   ├── RemoveItemCommand             ├── CartItemDto
│   │   ├── ClearCartCommand              ├── CartSnapshotDto
│   │   ├── ApplyPromoCodeCommand         ├── DimensionsDto
│   │   ├── RemovePromoCodeCommand        ├── SelectedOptionDto
│   │   ├── RefreshPricesCommand          ├── BomLineDto
│   │   ├── MergeCartsCommand             ├── AppliedPromoCodeDto
│   │   ├── CreateSnapshotCommand         ├── AddItemResult
│   │   ├── ApplyPromoCodeResult          ├── MergeCartsResult
│   │   ├── RefreshPricesResult           └── CreateSnapshotResult
│   │
│   ├── port/
│   │   ├── in/                           # Inbound Ports - Use Cases (10)
│   │   │   ├── GetCartUseCase
│   │   │   ├── AddItemUseCase
│   │   │   ├── UpdateItemUseCase
│   │   │   ├── RemoveItemUseCase
│   │   │   ├── ClearCartUseCase
│   │   │   ├── ApplyPromoCodeUseCase
│   │   │   ├── RemovePromoCodeUseCase
│   │   │   ├── RefreshPricesUseCase
│   │   │   ├── MergeCartsUseCase
│   │   │   └── CreateSnapshotUseCase
│   │   │
│   │   └── out/                          # Outbound Ports (9)
│   │       ├── CartRepository
│   │       ├── CartSnapshotRepository
│   │       ├── CartEventPublisher
│   │       ├── EventPublisher
│   │       ├── SessionCartStore
│   │       ├── CatalogConfigurationPort
│   │       ├── CatalogConfigurationClient
│   │       ├── PricingPort
│   │       └── PricingClient
│   │
│   └── service/                          # Application Services (7)
│       ├── CartApplicationService        # Orchestrator
│       ├── CartCheckoutService
│       ├── CartItemValidationService
│       ├── CartMergingService
│       ├── CartPricingService
│       ├── CartPromoCodeService
│       └── CartDtoMapper
│
├── domain/                               # Domain Layer (38 files)
│   ├── model/                            # Aggregates & Value Objects (13)
│   │   ├── Cart                          # Aggregate Root
│   │   ├── CartItem                      # Entity
│   │   ├── CartSnapshot                  # Aggregate
│   │   ├── CartSnapshotItem              # Entity
│   │   ├── CartId                        # Value Object
│   │   ├── CartItemId                    # Value Object
│   │   ├── SnapshotId                    # Value Object
│   │   ├── CartStatus                    # Enum (ACTIVE, CHECKED_OUT, ABANDONED, MERGED)
│   │   ├── CartTotals                    # Value Object
│   │   ├── ConfigurationSnapshot         # Value Object
│   │   ├── PriceQuoteReference           # Value Object
│   │   ├── AppliedPromoCode              # Value Object
│   │   └── ValidationStatus              # Enum (VALID, INVALID, UNKNOWN)
│   │
│   ├── event/                            # Domain Events (11)
│   │   ├── CartCreatedEvent
│   │   ├── CartItemAddedEvent
│   │   ├── CartItemUpdatedEvent
│   │   ├── CartItemRemovedEvent
│   │   ├── CartClearedEvent
│   │   ├── CartMergedEvent
│   │   ├── CartAbandonedEvent
│   │   ├── CartCheckedOutEvent
│   │   ├── CartPricesRefreshedEvent
│   │   ├── PromoCodeAppliedEvent
│   │   └── PromoCodeRemovedEvent
│   │
│   ├── service/                          # Domain Services (3)
│   │   ├── CartMergeService
│   │   ├── CartTotalsCalculator
│   │   └── ConfigurationHashService
│   │
│   └── exception/                        # Domain Exceptions (10)
│       ├── CartDomainErrors              ├── CartNotFoundException
│       ├── CartItemNotFoundException     ├── InvalidQuantityException
│       ├── InvalidConfigurationException ├── PromoCodeInvalidException
│       ├── PromoCodeMinimumNotMetException
│       ├── EmptyCartException            ├── AnonymousCheckoutException
│       ├── InvalidItemsException         └── SnapshotExpiredException
│
└── adapters/                             # Infrastructure Layer (24 files)
    ├── in/grpc/                          # gRPC Adapter (3)
    │   ├── CartGrpcService
    │   ├── CartGrpcMapper
    │   └── GrpcExceptionAdvice
    │
    ├── out/                              # Outbound Adapters
    │   ├── persistence/                  # JPA (8)
    │   │   ├── CartJpaEntity
    │   │   ├── CartItemJpaEntity
    │   │   ├── CartSnapshotJpaEntity
    │   │   ├── CartJpaRepository
    │   │   ├── CartSnapshotJpaRepository
    │   │   ├── CartRepositoryAdapter
    │   │   ├── CartSnapshotRepositoryAdapter
    │   │   └── CartPersistenceMapper
    │   │
    │   ├── grpc/                         # gRPC Clients (2)
    │   │   ├── CatalogConfigurationGrpcClient
    │   │   └── PricingGrpcClient
    │   │
    │   ├── kafka/                        # Kafka (1)
    │   │   └── CartKafkaEventPublisher
    │   │
    │   └── redis/                        # Redis (1)
    │       └── RedisSessionCartStore
    │
    └── config/                           # Configuration (9)
        ├── CartServiceConfig
        ├── CartProperties
        ├── GrpcConfig
        ├── GrpcClientConfig
        ├── KafkaConfig
        ├── PersistenceConfig
        ├── RedisConfig
        ├── ResilienceConfig
        └── SecurityConfig
```

---

## API Endpoints

### gRPC (port 9082)

| Method | Description | FC Contract |
|--------|-------------|-------------|
| GetCart | Get or create cart | FC-cart-getCart |
| AddItem | Add item to cart | FC-cart-addItem |
| UpdateItem | Update item quantity | FC-cart-updateItem |
| RemoveItem | Remove item | FC-cart-removeItem |
| ClearCart | Clear all items | FC-cart-clearCart |
| ApplyPromoCode | Apply promo | FC-cart-applyPromoCode |
| RemovePromoCode | Remove promo | FC-cart-removePromoCode |
| RefreshPrices | Refresh all prices | FC-cart-refreshPrices |
| MergeCarts | Merge anonymous cart | FC-cart-mergeCarts |
| CreateSnapshot | Create checkout snapshot | FC-cart-createSnapshot |

---

## Domain Events → Kafka Topics

| Event | Topic | Consumers |
|-------|-------|-----------|
| CartCreatedEvent | cart-events | analytics-service |
| CartItemAddedEvent | cart-events | analytics-service |
| CartItemUpdatedEvent | cart-events | analytics-service |
| CartItemRemovedEvent | cart-events | analytics-service |
| CartClearedEvent | cart-events | analytics-service |
| CartMergedEvent | cart-events | analytics-service |
| CartAbandonedEvent | cart-events | notification-service |
| CartCheckedOutEvent | cart-events | order-service |
| CartPricesRefreshedEvent | cart-events | analytics-service |
| PromoCodeAppliedEvent | cart-events | analytics-service, reporting-service |
| PromoCodeRemovedEvent | cart-events | analytics-service |

---

## Dependencies

### Upstream (consumed by this service)
- **catalog-configuration-service**: Configuration validation
- **pricing-service**: Price quotes, promo code validation

### Downstream (this service produces to)
- **PostgreSQL** (`carts` schema): Cart persistence
- **Redis**: Session cart storage, cache
- **Kafka** (cart-events): Domain event publishing

---

## Links

- [RequirementsAnalysis.xml#UC-CART-MANAGE](file:///c:/windows-store-project/kanokna-copy/backend/windows-store-server/docs/grace/RequirementsAnalysis.xml)
- [DevelopmentPlan.xml#DP-SVC-cart-service](file:///c:/windows-store-project/kanokna-copy/backend/windows-store-server/docs/grace/DevelopmentPlan.xml)
- [Technology.xml#DEC-CART-STATE-MACHINE](file:///c:/windows-store-project/kanokna-copy/backend/windows-store-server/docs/grace/Technology.xml)
- [cart_service.proto](file:///c:/windows-store-project/kanokna-copy/backend/windows-store-server/api-contracts/proto/kanokna/cart/v1/cart_service.proto)
