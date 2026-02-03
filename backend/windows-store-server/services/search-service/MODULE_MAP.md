# MODULE_MAP: search-service

**Service:** search-service  
**Package:** `com.kanokna.search`  
**Wave:** W1 (CPQ Core)  
**Task:** W1-T5

---

## Contracts

| Type | ID | File |
|------|-----|------|
| FC | FC-search-searchProducts | [FC-search-service-functions.xml](file:///c:/windows-store-project/kanokna-copy/backend/windows-store-server/docs/grace/contracts/FC-search-service-functions.xml) |
| FC | FC-search-autocomplete | ^ |
| FC | FC-search-indexProduct | ^ |
| FC | FC-search-deleteProduct | ^ |
| FC | FC-search-getFacetValues | ^ |
| FC | FC-search-getProductById | ^ |
| FC | FC-search-reindexAll | ^ |
| MC | MC-search-service-domain | [MC-search-service-domain.xml](file:///c:/windows-store-project/kanokna-copy/backend/windows-store-server/docs/grace/contracts/MC-search-service-domain.xml) |

---

## BlockAnchors

| Prefix | Count | Purpose |
|--------|-------|---------|
| BA-SEARCH-QUERY-* | 4 | Search query execution |
| BA-AUTO-* | 4 | Autocomplete suggestions |
| BA-INDEX-* | 4 | Product indexing |
| BA-DELETE-* | 3 | Product deletion |
| BA-FACET-* | 4 | Facet aggregation |
| BA-GET-* | 3 | Get by ID |
| BA-REINDEX-* | 6 | Full reindex flow |

---

## Package Structure

```
com.kanokna.search
├── application/
│   └── service/         # SearchApplicationService
├── domain/
│   ├── model/           # ProductSearchDocument
│   └── query/           # SearchQuery, FacetQuery
├── infrastructure/
│   ├── elasticsearch/   # ES client, index management
│   ├── kafka/           # Catalog event consumer
│   └── redis/           # Reindex distributed lock
└── api/
    └── grpc/            # SearchServiceGrpc
```
