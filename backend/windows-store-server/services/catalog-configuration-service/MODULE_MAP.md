# MODULE_MAP: catalog-configuration-service

**Service:** catalog-configuration-service  
**Package:** `com.kanokna.catalog`  
**Wave:** W1 (CPQ Core)  
**Task:** W1-T1

---

## Contracts

| Type | ID | File |
|------|-----|------|
| FC | FC-catalog-configuration-validateConfiguration | [FC-catalog-configuration-service-validateConfiguration.xml](file:///c:/windows-store-project/kanokna-copy/backend/windows-store-server/docs/grace/contracts/FC-catalog-configuration-service-validateConfiguration.xml) |
| MC | MC-catalog-configuration-service-domain | [MC-catalog-configuration-service-domain-ConfigurationValidation.xml](file:///c:/windows-store-project/kanokna-copy/backend/windows-store-server/docs/grace/contracts/MC-catalog-configuration-service-domain-ConfigurationValidation.xml) |

---

## BlockAnchors

| ID | Purpose |
|----|---------|
| BA-CFG-VAL-01 | Check dimension constraints |
| BA-CFG-VAL-02 | Check material/glazing compatibility |
| BA-CFG-VAL-03 | Check option dependencies |
| BA-CFG-VAL-04 | Apply auto-corrections |
| BA-CFG-VAL-99 | Final validation result |
| BA-CAT-CREATE-01 | Validate admin authorization for create |
| BA-CAT-CREATE-02 | Validate uniqueness constraints |
| BA-CAT-CREATE-03 | Build ProductTemplate aggregate |
| BA-CAT-CREATE-04 | Persist and return ID |
| BA-CAT-UPDATE-* | Template update flow |
| BA-CAT-PUBLISH-* | Catalog publish flow |

---

## Package Structure

```
com.kanokna.catalog
├── application/
│   └── service/         # ConfigurationApplicationService
├── domain/
│   ├── model/           # ProductTemplate, OptionGroup, Rule
│   ├── validation/      # ValidationEngine
│   └── event/           # CatalogPublishedEvent
├── infrastructure/
│   ├── persistence/     # JPA repositories
│   └── cache/           # Redis catalog cache
└── api/
    └── grpc/            # CatalogConfigurationServiceGrpc
```
