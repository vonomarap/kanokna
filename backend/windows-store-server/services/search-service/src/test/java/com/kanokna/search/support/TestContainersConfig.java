package com.kanokna.search.support;

import com.kanokna.test.containers.elasticsearch.ElasticsearchTestContainer;
import com.kanokna.test.containers.kafka.KafkaTestContainer;
import com.kanokna.test.containers.redis.RedisTestContainer;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.lifecycle.Startables;

public abstract class TestContainersConfig {
    private static final String SCHEMA_REGISTRY_URL = "mock://search-service";
    private static final String SPRING_KAFKA_SCHEMA_REGISTRY =
        "spring.kafka.properties.schema.registry.url";
    private static final String SPRING_CLOUD_CONFIG_ENABLED = "spring.cloud.config.enabled";
    private static final String FALSE_VALUE = "false";

    protected static final ElasticsearchTestContainer ELASTICSEARCH = ElasticsearchTestContainer.instance();
    protected static final KafkaTestContainer KAFKA = KafkaTestContainer.instance();
    protected static final RedisTestContainer REDIS = RedisTestContainer.instance();

    private static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable ex) {
            return false;
        }
    }

    @BeforeAll
    static void startContainers() {
        Assumptions.assumeTrue(isDockerAvailable(),
            "Docker is not available, skipping integration tests");
        Startables.deepStart(
            ELASTICSEARCH.container(),
            KAFKA.container(),
            REDIS.container()
        ).join();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        if (!isDockerAvailable()) {
            return;
        }
        ELASTICSEARCH.registerProperties(registry);
        KAFKA.registerProperties(registry);
        REDIS.registerProperties(registry);
        registry.add(SPRING_KAFKA_SCHEMA_REGISTRY, () -> SCHEMA_REGISTRY_URL);
        registry.add(SPRING_CLOUD_CONFIG_ENABLED, () -> FALSE_VALUE);
    }
}
