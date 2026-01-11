package com.kanokna.account.adapters.out.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserProfileJpaRepositoryTest {
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
    private UserProfileJpaRepository repository;

    @Test
    @DisplayName("DataJpa: save and find profile")
    void saveAndFindProfile() {
        UUID id = UUID.randomUUID();
        UserProfileJpaEntity entity = new UserProfileJpaEntity();
        entity.setId(id);
        entity.setEmail("user@example.com");
        entity.setVersion(0);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        repository.save(entity);

        UserProfileJpaEntity loaded = repository.findById(id).orElseThrow();
        assertEquals("user@example.com", loaded.getEmail());
    }

    @Test
    @DisplayName("DataJpa: version increments on update")
    void versionIncrementsOnUpdate() {
        UUID id = UUID.randomUUID();
        UserProfileJpaEntity entity = new UserProfileJpaEntity();
        entity.setId(id);
        entity.setEmail("user@example.com");
        entity.setVersion(0);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        UserProfileJpaEntity saved = repository.save(entity);
        int initialVersion = saved.getVersion();

        saved.setFirstName("Updated");
        UserProfileJpaEntity updated = repository.save(saved);
        assertTrue(updated.getVersion() >= initialVersion);
    }
}
