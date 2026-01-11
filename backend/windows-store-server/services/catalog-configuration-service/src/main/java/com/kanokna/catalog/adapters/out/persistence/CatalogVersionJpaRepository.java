package com.kanokna.catalog.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for CatalogVersion.
 */
@Repository
public interface CatalogVersionJpaRepository extends JpaRepository<CatalogVersionJpaEntity, UUID> {

    @Query("SELECT cv FROM CatalogVersionJpaEntity cv ORDER BY cv.versionNumber DESC LIMIT 1")
    Optional<CatalogVersionJpaEntity> findLatest();

    @Query("SELECT COALESCE(MAX(cv.versionNumber), 0) + 1 FROM CatalogVersionJpaEntity cv")
    int getNextVersionNumber();
}
