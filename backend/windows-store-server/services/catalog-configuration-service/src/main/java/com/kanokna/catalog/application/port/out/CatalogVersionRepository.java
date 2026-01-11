package com.kanokna.catalog.application.port.out;

import com.kanokna.catalog.domain.model.CatalogVersion;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: Catalog version persistence.
 */
public interface CatalogVersionRepository {

    CatalogVersion save(CatalogVersion catalogVersion);

    Optional<CatalogVersion> findById(UUID id);

    Optional<CatalogVersion> findLatest();

    int getNextVersionNumber();
}
