package com.kanokna.catalog.adapters.out.persistence;

import com.kanokna.catalog.application.port.out.CatalogVersionRepository;
import com.kanokna.catalog.domain.model.CatalogVersion;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing CatalogVersionRepository port.
 */
@Component
public class CatalogVersionRepositoryAdapter implements CatalogVersionRepository {

    private final CatalogVersionJpaRepository jpaRepository;

    public CatalogVersionRepositoryAdapter(CatalogVersionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CatalogVersion save(CatalogVersion catalogVersion) {
        CatalogVersionJpaEntity entity = new CatalogVersionJpaEntity();
        entity.setId(catalogVersion.id());
        entity.setVersionNumber(catalogVersion.versionNumber());
        entity.setPublishedAt(catalogVersion.publishedAt());
        entity.setPublishedBy(catalogVersion.publishedBy());
        entity.setSnapshot(catalogVersion.snapshot());

        CatalogVersionJpaEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<CatalogVersion> findById(UUID id) {
        return jpaRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public Optional<CatalogVersion> findLatest() {
        return jpaRepository.findLatest().map(this::mapToDomain);
    }

    @Override
    public int getNextVersionNumber() {
        return jpaRepository.getNextVersionNumber();
    }

    private CatalogVersion mapToDomain(CatalogVersionJpaEntity entity) {
        return new CatalogVersion(
            entity.getId(),
            entity.getVersionNumber(),
            entity.getPublishedAt(),
            entity.getPublishedBy(),
            entity.getSnapshot()
        );
    }
}
