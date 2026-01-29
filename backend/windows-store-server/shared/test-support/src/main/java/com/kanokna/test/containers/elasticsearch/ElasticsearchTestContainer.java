package com.kanokna.test.containers.elasticsearch;

import java.time.Duration;
import java.util.Objects;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Elasticsearch Testcontainers configuration with security disabled.
 */
public final class ElasticsearchTestContainer {
    private static final DockerImageName ELASTICSEARCH_IMAGE =
        DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.17.1");
    private static final int ELASTICSEARCH_PORT = 9200;
    private static final String XPACK_SECURITY_ENABLED = "xpack.security.enabled";
    private static final String XPACK_TRANSPORT_SSL_ENABLED = "xpack.security.transport.ssl.enabled";
    private static final String DISCOVERY_TYPE = "discovery.type";
    private static final String SINGLE_NODE = "single-node";
    private static final String ACTION_DESTRUCTIVE_REQUIRES_NAME = "action.destructive_requires_name";
    private static final String ES_JAVA_OPTS = "ES_JAVA_OPTS";
    private static final String ES_JAVA_OPTS_VALUE = "-Xms512m -Xmx512m";
    private static final String FALSE_VALUE = "false";
    private static final String SPRING_ELASTICSEARCH_URIS = "spring.elasticsearch.uris";

    private static final ElasticsearchTestContainer INSTANCE = new ElasticsearchTestContainer();

    private final ElasticsearchContainer container;

    private ElasticsearchTestContainer() {
        container = new ElasticsearchContainer(ELASTICSEARCH_IMAGE)
            .withEnv(XPACK_SECURITY_ENABLED, FALSE_VALUE)
            .withEnv(XPACK_TRANSPORT_SSL_ENABLED, FALSE_VALUE)
            .withEnv(DISCOVERY_TYPE, SINGLE_NODE)
            // Integration tests reset indices via prefix patterns (e.g. my_index_*).
            // Disable destructive requires name to allow wildcard deletes in the test container.
            .withEnv(ACTION_DESTRUCTIVE_REQUIRES_NAME, FALSE_VALUE)
            .withEnv(ES_JAVA_OPTS, ES_JAVA_OPTS_VALUE)
            // Log-based wait strategy can be flaky across Elasticsearch versions; use HTTP readiness instead.
            .waitingFor(Wait.forHttp("/_cluster/health?wait_for_status=yellow&timeout=60s")
                .forPort(ELASTICSEARCH_PORT)
                .forStatusCode(200)
                .withStartupTimeout(Duration.ofMinutes(3)))
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
