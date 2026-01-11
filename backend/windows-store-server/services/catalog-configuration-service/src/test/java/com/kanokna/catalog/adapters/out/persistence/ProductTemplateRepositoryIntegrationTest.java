package com.kanokna.catalog.adapters.out.persistence;

import com.kanokna.catalog.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ProductTemplateRepositoryAdapter with Testcontainers.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ProductTemplateRepositoryIntegrationTest {

    @Autowired
    private ProductTemplateJpaRepository jpaRepository;

    private ProductTemplateRepositoryAdapter adapter;

    @Test
    @DisplayName("Save and find product template")
    void saveAndFind_WorksCorrectly() {
        // Given
        adapter = new ProductTemplateRepositoryAdapter(jpaRepository);
        ProductTemplate template = ProductTemplate.create(
            "Test Window",
            "Description",
            ProductFamily.WINDOW,
            DimensionConstraints.standard()
        );

        // When
        ProductTemplate saved = adapter.save(template);
        ProductTemplate found = adapter.findById(saved.getId()).orElse(null);

        // Then
        assertNotNull(found);
        assertEquals("Test Window", found.getName());
        assertEquals(ProductFamily.WINDOW, found.getProductFamily());
    }

    @Test
    @DisplayName("Find by status returns correct templates")
    void findByStatus_ReturnsCorrectTemplates() {
        // Given
        adapter = new ProductTemplateRepositoryAdapter(jpaRepository);
        ProductTemplate template1 = ProductTemplate.create(
            "Draft 1",
            "Description",
            ProductFamily.WINDOW,
            DimensionConstraints.standard()
        );
        adapter.save(template1);

        ProductTemplate template2 = ProductTemplate.create(
            "Draft 2",
            "Description",
            ProductFamily.DOOR,
            DimensionConstraints.standard()
        );
        template2.publish();
        adapter.save(template2);

        // When
        List<ProductTemplate> drafts = adapter.findByStatus(TemplateStatus.DRAFT);
        List<ProductTemplate> active = adapter.findByStatus(TemplateStatus.ACTIVE);

        // Then
        assertTrue(drafts.size() >= 1);
        assertTrue(active.size() >= 1);
    }
}
