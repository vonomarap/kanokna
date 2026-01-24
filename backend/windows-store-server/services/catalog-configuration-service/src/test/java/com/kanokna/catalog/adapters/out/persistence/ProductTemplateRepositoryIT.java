package com.kanokna.catalog.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.kanokna.catalog.domain.model.DimensionConstraints;
import com.kanokna.catalog.domain.model.ProductFamily;
import com.kanokna.catalog.domain.model.ProductTemplate;
import com.kanokna.catalog.domain.model.TemplateStatus;

/**
 * Integration tests for ProductTemplateRepositoryAdapter with Testcontainers.
 */
@EnabledIf(
        value = "com.kanokna.catalog.support.DockerAvailability#isDockerAvailable",
        disabledReason = "Docker is not available, skipping Testcontainers integration tests"
)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ProductTemplateRepositoryIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("catalog_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        // Enable schema (namespace) creation for Hibernate
        registry.add("spring.jpa.properties.hibernate.hbm2ddl.create_namespaces", () -> "true");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "catalog_configuration");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.cloud.config.enabled", () -> "false");
    }

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
                DimensionConstraints.standard());

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
                DimensionConstraints.standard());
        adapter.save(template1);

        ProductTemplate template2 = ProductTemplate.create(
                "Draft 2",
                "Description",
                ProductFamily.DOOR,
                DimensionConstraints.standard());
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
