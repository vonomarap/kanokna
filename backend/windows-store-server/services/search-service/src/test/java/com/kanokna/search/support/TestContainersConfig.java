package com.kanokna.search.support;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

public abstract class TestContainersConfig {

    protected static final ElasticsearchContainer ELASTICSEARCH;
    protected static final KafkaContainer KAFKA;
    protected static final GenericContainer<?> REDIS;

    static {
        if (isDockerAvailable()) {
            ELASTICSEARCH = new ElasticsearchContainer(
                DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.12.2"))
                .withEnv("xpack.security.enabled", "false")
                .withEnv("xpack.security.transport.ssl.enabled", "false");
            
            KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));
            
            REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379);
        } else {
            ELASTICSEARCH = null;
            KAFKA = null;
            REDIS = null;
        }
    }

    private static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable ex) {
            return false;
        }
    }

    @BeforeAll
    static void startContainers() {
        Assumptions.assumeTrue(ELASTICSEARCH != null, "Docker is not available, skipping integration tests");
        Startables.deepStart(ELASTICSEARCH, KAFKA, REDIS).join();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        if (ELASTICSEARCH == null) {
            return;
        }
        registry.add("spring.elasticsearch.uris", ELASTICSEARCH::getHttpHostAddress);
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.kafka.properties.schema.registry.url", () -> "mock://search-service");
        registry.add("spring.redis.host", REDIS::getHost);
        registry.add("spring.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("spring.cloud.config.enabled", () -> "false");
    }
}
