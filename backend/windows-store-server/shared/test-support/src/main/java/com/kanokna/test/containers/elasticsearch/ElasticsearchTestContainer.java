package com.kanokna.test.containers.elasticsearch;

import java.util.Objects;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Elasticsearch Testcontainers configuration with security disabled.
 */
public final class ElasticsearchTestContainer {
    private static final DockerImageName ELASTICSEARCH_IMAGE =
        DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.12.2");
    private static final String XPACK_SECURITY_ENABLED = "xpack.security.enabled";
    private static final String XPACK_TRANSPORT_SSL_ENABLED = "xpack.security.transport.ssl.enabled";
    private static final String FALSE_VALUE = "false";
    private static final String SPRING_ELASTICSEARCH_URIS = "spring.elasticsearch.uris";

    private static final ElasticsearchTestContainer INSTANCE = new ElasticsearchTestContainer();

    private final ElasticsearchContainer container;

    private ElasticsearchTestContainer() {
        container = new ElasticsearchContainer(ELASTICSEARCH_IMAGE)
            .withEnv(XPACK_SECURITY_ENABLED, FALSE_VALUE)
            .withEnv(XPACK_TRANSPORT_SSL_ENABLED, FALSE_VALUE)
            .withReuse(true);
    }

    public static ElasticsearchTestContainer instance() {
        return INSTANCE;
    }

    public ElasticsearchContainer container() {
        return container;
    }

    public void startIfNeeded() {
        if (!container.isRunning()) {
            container.start();
        }
    }

    public String httpUrl() {
        startIfNeeded();
        return container.getHttpHostAddress();
    }

    public void registerProperties(DynamicPropertyRegistry registry) {
        Objects.requireNonNull(registry, "registry");
        registry.add(SPRING_ELASTICSEARCH_URIS, this::httpUrl);
    }
}
