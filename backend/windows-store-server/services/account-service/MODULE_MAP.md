# MODULE_MAP: account-service

**Service:** account-service  
**Package:** `com.kanokna.account`  
**Wave:** W1 (CPQ Core)  
**Task:** W1-T2  
**Schema Version:** grace-markup-v2

---

## Contracts

### Function Contracts (6 total)

| ID | Intent | Location |
|----|--------|----------|
| FC-account-getProfile | Retrieve user profile, auto-create from Keycloak on first access | [FC-account-service-functions.xml](file:///c:/windows-store-project/kanokna-copy/backend/windows-store-server/docs/grace/contracts/FC-account-service-functions.xml) |
| FC-account-updateProfile | Update profile fields (name, phone, preferences) | ^ |
| FC-account-manageAddresses | CRUD for delivery/installation addresses | ^ |
| FC-account-saveConfiguration | Save product configuration as draft | ^ |
| FC-account-listConfigurations | List all saved configurations for a user | ^ |
| FC-account-deleteConfiguration | Delete a saved product configuration | ^ |

### Module Contracts (8 total)

| ID | Layer | Location |
|----|-------|----------|
| MC-account-service-domain | domain | [MC-account-service-domain.xml](file:///c:/windows-store-project/kanokna-copy/backend/windows-store-server/docs/grace/contracts/MC-account-service-domain.xml) |
| MC-account-profile-service | application.service | [ProfileService.java](file:///c:/windows-store-project/kanokna-copy/backend/windows-store-server/services/account-service/src/main/java/com/kanokna/account/application/service/ProfileService.java#L178-183) |
| MC-account-address-service | application.service | [AddressService.java](file:///c:/windows-store-project/kanokna-copy/backend/windows-store-server/services/account-service/src/main/java/com/kanokna/account/application/service/AddressService.java#L131-136) |
| MC-account-savedconfig-service | application.service | [SavedConfigurationService.java](file:///c:/windows-store-project/kanokna-copy/backend/windows-store-server/services/account-service/src/main/java/com/kanokna/account/application/service/SavedConfigurationService.java#L121-126) |
| MC-account-grpc-adapter | adapters.in.grpc | [AccountGrpcService.java](file:///c:/windows-store-project/kanokna-copy/backend/windows-store-server/services/account-service/src/main/java/com/kanokna/account/adapters/in/grpc/AccountGrpcService.java) |
| MC-account-web-adapter | adapters.in.web | *Controller.java |
| MC-account-persistence-adapter | adapters.out.persistence | *RepositoryAdapter.java |
| MC-account-keycloak-adapter | adapters.out.keycloak | [KeycloakCurrentUserProvider.java](file:///c:/windows-store-project/kanokna-copy/backend/windows-store-server/services/account-service/src/main/java/com/kanokna/account/adapters/out/keycloak/KeycloakCurrentUserProvider.java) |

---

## BlockAnchors (13 total)

### Profile Anchors
| ID | Purpose | Service |
|----|---------|---------|
| BA-ACCT-PROF-01 | Fetch user profile from repository | ProfileService |
| BA-ACCT-PROF-02 | Validate and apply profile updates | ProfileService |
| BA-ACCT-PROF-03 | Sync profile from Keycloak on first login | ProfileService |

### Address Anchors
| ID | Purpose | Service |
|----|---------|---------|
| BA-ACCT-ADDR-01 | Validate address input | AddressService |
| BA-ACCT-ADDR-02 | Check for duplicate address | AddressService |
| BA-ACCT-ADDR-03 | Update default address flag | AddressService |
| BA-ACCT-ADDR-04 | List user addresses | AddressService |

### Configuration Anchors
| ID | Purpose | Service |
|----|---------|---------|
| BA-ACCT-CFG-01 | Validate configuration snapshot structure | SavedConfigurationService |
| BA-ACCT-CFG-02 | Persist saved configuration | SavedConfigurationService |
| BA-ACCT-CFG-03 | Emit ConfigurationSavedEvent | SavedConfigurationService |
| BA-ACCT-CFG-04 | List saved configurations | SavedConfigurationService |
| BA-ACCT-CFG-05 | Delete saved configuration | SavedConfigurationService |

### Cross-Cutting Anchors
| ID | Purpose | Service |
|----|---------|---------|
| BA-ACCT-AUTH-01 | Verify caller authorization | All services |

---

## Package Structure

```
com.kanokna.account
├── application/                          # Application Layer (30 files)
│   ├── dto/                              # Commands, Queries, DTOs (14)
│   │   ├── AddAddressCommand             ├── GetProfileQuery
│   │   ├── UpdateAddressCommand          ├── ListAddressesQuery
│   │   ├── DeleteAddressCommand          ├── ListConfigurationsQuery
│   │   ├── UpdateProfileCommand          ├── UserProfileDto
│   │   ├── SaveConfigurationCommand      ├── SavedAddressDto
│   │   ├── DeleteConfigurationCommand    ├── SavedConfigurationDto
│   │   ├── AddressDto                    └── NotificationPreferencesDto
│   │
│   ├── port/
│   │   ├── in/                           # Inbound Ports - Use Cases (4)
│   │   │   ├── GetProfileUseCase
│   │   │   ├── UpdateProfileUseCase
│   │   │   ├── AddressManagementUseCase
│   │   │   └── ConfigurationManagementUseCase
│   │   │
│   │   └── out/                          # Outbound Ports - Repositories (6)
│   │       ├── UserProfileRepository
│   │       ├── SavedAddressRepository
│   │       ├── SavedConfigurationRepository
│   │       ├── CurrentUserProvider
│   │       ├── CurrentUser
│   │       └── EventPublisher
│   │
│   └── service/                          # Application Services (6)
│       ├── AccountApplicationService     # Facade implementing all use cases
│       ├── ProfileService                # Profile CRUD operations
│       ├── AddressService                # Address CRUD operations
│       ├── SavedConfigurationService     # Configuration CRUD operations
│       └── AccountServiceUtils           # Shared utilities
│
├── domain/                               # Domain Layer (17 files)
│   ├── model/                            # Aggregates & Value Objects (9)
│   │   ├── UserProfile                   # Aggregate Root
│   │   ├── SavedAddress                  # Entity
│   │   ├── SavedConfiguration            # Aggregate
│   │   ├── PersonName                    # Value Object
│   │   ├── LocalePreference              # Value Object
│   │   ├── CurrencyPreference            # Value Object
│   │   ├── NotificationPreferences       # Value Object
│   │   ├── ConfigurationSnapshot         # Value Object
│   │   └── QuoteSnapshot                 # Value Object
│   │
│   ├── event/                            # Domain Events (7)
│   │   ├── UserProfileCreatedEvent
│   │   ├── UserProfileUpdatedEvent
│   │   ├── AddressAddedEvent
│   │   ├── AddressUpdatedEvent
│   │   ├── AddressDeletedEvent
│   │   ├── ConfigurationSavedEvent
│   │   └── ConfigurationDeletedEvent
│   │
│   └── exception/                        # Domain Exceptions (1)
│       └── AccountDomainErrors
│
└── adapters/                             # Infrastructure Layer (23 files)
    ├── in/                               # Inbound Adapters (7)
    │   ├── grpc/
    │   │   ├── AccountGrpcService        # gRPC implementation
    │   │   ├── AccountGrpcMapper
    │   │   └── GrpcExceptionAdvice
    │   │
    │   └── web/                          # REST Controllers
    │       ├── AccountProfileController
    │       ├── AccountAddressController
    │       ├── AccountConfigurationController
    │       └── GlobalExceptionHandler
    │
    ├── out/                              # Outbound Adapters (11)
    │   ├── persistence/
    │   │   ├── UserProfileJpaEntity
    │   │   ├── UserProfileJpaRepository
    │   │   ├── UserProfileRepositoryAdapter
    │   │   ├── SavedAddressJpaEntity
    │   │   ├── SavedAddressJpaRepository
    │   │   ├── SavedAddressRepositoryAdapter
    │   │   ├── SavedConfigurationJpaEntity
    │   │   ├── SavedConfigurationJpaRepository
    │   │   ├── SavedConfigurationRepositoryAdapter
    │   │   └── AccountPersistenceMapper
    │   │
    │   └── keycloak/
    │       └── KeycloakCurrentUserProvider
    │
    └── config/                           # Configuration (5)
        ├── AccountServiceConfig
        ├── AccountProperties
        ├── SecurityConfig
        ├── PersistenceConfig
        └── GrpcConfig
```

---

## API Endpoints

### gRPC (port 9085)

| Method | Request | Response |
|--------|---------|----------|
| GetProfile | GetProfileRequest | GetProfileResponse |
| UpdateProfile | UpdateProfileRequest | UpdateProfileResponse |
| AddAddress | AddAddressRequest | AddAddressResponse |
| UpdateAddress | UpdateAddressRequest | UpdateAddressResponse |
| DeleteAddress | DeleteAddressRequest | DeleteAddressResponse |
| ListAddresses | ListAddressesRequest | ListAddressesResponse |
| GetOrderHistory | GetOrderHistoryRequest | GetOrderHistoryResponse |

### REST (port 8085)

| Method | Endpoint | FC Contract |
|--------|----------|-------------|
| GET | /api/accounts/{userId}/profile | FC-account-getProfile |
| PUT | /api/accounts/{userId}/profile | FC-account-updateProfile |
| GET | /api/accounts/{userId}/addresses | FC-account-manageAddresses |
| POST | /api/accounts/{userId}/addresses | FC-account-manageAddresses |
| PUT | /api/accounts/{userId}/addresses/{addressId} | FC-account-manageAddresses |
| DELETE | /api/accounts/{userId}/addresses/{addressId} | FC-account-manageAddresses |
| GET | /api/accounts/{userId}/configurations | FC-account-listConfigurations |
| POST | /api/accounts/{userId}/configurations | FC-account-saveConfiguration |
| DELETE | /api/accounts/{userId}/configurations/{configurationId} | FC-account-deleteConfiguration |

---

## Domain Events

| Event | Payload | Consumers |
|-------|---------|-----------|
| UserProfileCreatedEvent | userId, email, createdAt | notification-service, reporting-service |
| UserProfileUpdatedEvent | userId, changedFields, updatedAt | reporting-service |
| AddressAddedEvent | userId, addressId, isDefault | - |
| AddressUpdatedEvent | userId, addressId, changedFields | - |
| AddressDeletedEvent | userId, addressId | - |
| ConfigurationSavedEvent | userId, configId, productTemplateId, savedAt | reporting-service |
| ConfigurationDeletedEvent | userId, configurationId | - |

---

## Dependencies

### Upstream (consumed by this service)
- **Keycloak**: JWT authentication, user claims (sub, email, name)

### Downstream (this service produces to)
- **PostgreSQL** (`accounts` schema): Persistent storage
- **Kafka** (async): Domain events publishing

---

## Links

- [RequirementsAnalysis.xml#UC-ACCOUNT-MANAGE-PROFILE](file:///c:/windows-store-project/kanokna-copy/backend/windows-store-server/docs/grace/RequirementsAnalysis.xml)
- [DevelopmentPlan.xml#DP-SVC-account-service](file:///c:/windows-store-project/kanokna-copy/backend/windows-store-server/docs/grace/DevelopmentPlan.xml)
- [Technology.xml#TECH-postgresql](file:///c:/windows-store-project/kanokna-copy/backend/windows-store-server/docs/grace/Technology.xml)
- [account_service.proto](file:///c:/windows-store-project/kanokna-copy/backend/windows-store-server/api-contracts/proto/kanokna/account/v1/account_service.proto)
