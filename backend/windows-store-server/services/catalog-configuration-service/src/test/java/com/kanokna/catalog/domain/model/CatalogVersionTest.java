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
        assertNotNull(catalogVersion.getId());
        assertEquals(1, catalogVersion.getVersionNumber());
        assertEquals("admin-user", catalogVersion.getPublishedBy());
        assertNotNull(catalogVersion.getPublishedAt());
        assertEquals("{\"templates\": []}", catalogVersion.getSnapshot());
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
