package com.kanokna.account.adapters.out.persistence;

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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIf(
    value = "com.kanokna.account.support.DockerAvailability#isDockerAvailable",
    disabledReason = "Docker is not available, skipping Testcontainers repository tests"
)
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SavedConfigurationJpaRepositoryTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("accounts")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.hikari.connection-init-sql", () -> "CREATE SCHEMA IF NOT EXISTS accounts");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private SavedConfigurationJpaRepository repository;

    @Test
    @DisplayName("DataJpa: find configurations by user id")
    void findByUserId() {
        UUID userId = UUID.randomUUID();
        SavedConfigurationJpaEntity entity = new SavedConfigurationJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setName("Config A");
        entity.setProductTemplateId(UUID.randomUUID());
        entity.setConfigurationSnapshot("{\"productTemplateId\":\"" + UUID.randomUUID() + "\"}");
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        repository.save(entity);

        List<SavedConfigurationJpaEntity> results = repository.findByUserId(userId);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("DataJpa: delete configuration by user and id")
    void deleteByUserIdAndId() {
        UUID userId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();
        SavedConfigurationJpaEntity entity = new SavedConfigurationJpaEntity();
        entity.setId(configId);
        entity.setUserId(userId);
        entity.setName("Config A");
        entity.setProductTemplateId(UUID.randomUUID());
        entity.setConfigurationSnapshot("{\"productTemplateId\":\"" + UUID.randomUUID() + "\"}");
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        repository.save(entity);

        repository.deleteByUserIdAndId(userId, configId);

        assertTrue(repository.findByUserId(userId).isEmpty());
    }
}
