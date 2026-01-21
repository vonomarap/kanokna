package com.kanokna.test.containers.postgres;

import java.util.Objects;
import org.flywaydb.core.Flyway;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Postgres Testcontainers configuration for integration tests.
 * Provides a singleton container and Spring property registration helpers.
 */
public final class PostgresTestContainer {
    private static final DockerImageName POSTGRES_IMAGE =
        DockerImageName.parse("postgres:16-alpine");
    private static final String DEFAULT_DATABASE = "test";
    private static final String DEFAULT_USERNAME = "test";
    private static final String DEFAULT_PASSWORD = "test";
    private static final String HIBERNATE_DDL_AUTO = "validate";
    private static final String TRUE_VALUE = "true";
    private static final String FALSE_VALUE = "false";
    private static final String SPRING_DATASOURCE_URL = "spring.datasource.url";
    private static final String SPRING_DATASOURCE_USERNAME = "spring.datasource.username";
    private static final String SPRING_DATASOURCE_PASSWORD = "spring.datasource.password";
    private static final String SPRING_JPA_DDL_AUTO = "spring.jpa.hibernate.ddl-auto";
    private static final String SPRING_JPA_DEFAULT_SCHEMA =
        "spring.jpa.properties.hibernate.default_schema";
    private static final String SPRING_FLYWAY_ENABLED = "spring.flyway.enabled";
    private static final String SPRING_FLYWAY_SCHEMAS = "spring.flyway.schemas";
    private static final String SPRING_CLOUD_CONFIG_ENABLED = "spring.cloud.config.enabled";
    private static final String FLYWAY_LOCATIONS = "classpath:db/migration";

    private static final PostgresTestContainer INSTANCE = new PostgresTestContainer();

    private final PostgreSQLContainer<?> container;

    private PostgresTestContainer() {
        container = new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withDatabaseName(DEFAULT_DATABASE)
            .withUsername(DEFAULT_USERNAME)
            .withPassword(DEFAULT_PASSWORD)
            .withReuse(true);
    }

    public static PostgresTestContainer instance() {
        return INSTANCE;
    }

    public PostgreSQLContainer<?> container() {
        return container;
    }

    public void startIfNeeded() {
        if (!container.isRunning()) {
            container.start();
        }
    }

    public void registerProperties(DynamicPropertyRegistry registry, PostgresSettings settings) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(settings, "settings");
        startIfNeeded();

        registry.add(SPRING_DATASOURCE_URL, container::getJdbcUrl);
        registry.add(SPRING_DATASOURCE_USERNAME, container::getUsername);
        registry.add(SPRING_DATASOURCE_PASSWORD, container::getPassword);
        registry.add(SPRING_JPA_DDL_AUTO, () -> HIBERNATE_DDL_AUTO);
        registry.add(SPRING_JPA_DEFAULT_SCHEMA, settings::schema);
        registry.add(SPRING_FLYWAY_ENABLED,
            () -> settings.flywayEnabled() ? TRUE_VALUE : FALSE_VALUE);
        if (settings.flywayEnabled()) {
            registry.add(SPRING_FLYWAY_SCHEMAS, settings::schema);
            migrateSchema(settings);
        }
        registry.add(SPRING_CLOUD_CONFIG_ENABLED, () -> FALSE_VALUE);
    }

    private void migrateSchema(PostgresSettings settings) {
        Flyway.configure()
            .dataSource(container.getJdbcUrl(), container.getUsername(), container.getPassword())
            .createSchemas(true)
            .defaultSchema(settings.schema())
            .schemas(settings.schema())
            .locations(FLYWAY_LOCATIONS)
            .load()
            .migrate();
    }

    public record PostgresSettings(String schema, boolean flywayEnabled) {
        public PostgresSettings {
            if (schema == null || schema.isBlank()) {
                throw new IllegalArgumentException("schema must be provided");
            }
        }

        public static PostgresSettings withSchema(String schema) {
            return new PostgresSettings(schema, true);
        }
    }
}
