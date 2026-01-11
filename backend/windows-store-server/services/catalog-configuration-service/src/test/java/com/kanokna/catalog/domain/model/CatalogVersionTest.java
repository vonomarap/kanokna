package com.kanokna.catalog.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CatalogVersion aggregate.
 */
class CatalogVersionTest {

    @Test
    @DisplayName("Create catalog version with valid data")
    void createCatalogVersion_Succeeds() {
        // When
        CatalogVersion catalogVersion = CatalogVersion.create(
            1,
            "admin-user",
            "{\"templates\": []}"
        );

        // Then
        assertNotNull(catalogVersion.id());
        assertEquals(1, catalogVersion.versionNumber());
        assertEquals("admin-user", catalogVersion.publishedBy());
        assertNotNull(catalogVersion.publishedAt());
        assertEquals("{\"templates\": []}", catalogVersion.snapshot());
    }

    @Test
    @DisplayName("Version number must be positive")
    void zeroVersionNumber_ThrowsException() {
        // When/Then
        assertThrows(IllegalArgumentException.class,
            () -> CatalogVersion.create(0, "user", "{}"));
    }

    @Test
    @DisplayName("Snapshot cannot be null")
    void nullSnapshot_ThrowsException() {
        // When/Then
        assertThrows(NullPointerException.class,
            () -> CatalogVersion.create(1, "user", null));
    }
}
