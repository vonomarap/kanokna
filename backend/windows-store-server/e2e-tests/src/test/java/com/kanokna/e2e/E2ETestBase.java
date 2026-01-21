package com.kanokna.e2e;

import com.kanokna.test.containers.elasticsearch.ElasticsearchTestContainer;
import com.kanokna.test.containers.kafka.KafkaTestContainer;
import com.kanokna.test.containers.postgres.PostgresTestContainer;
import com.kanokna.test.containers.redis.RedisTestContainer;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.lifecycle.Startables;

/**
 * Base class for E2E tests with shared infrastructure containers and routing helpers.
 */
public abstract class E2ETestBase {
    private static final String DOCKER_UNAVAILABLE_MESSAGE =
        "Docker is not available, skipping E2E tests";

    protected static final PostgresTestContainer POSTGRES = PostgresTestContainer.instance();
    protected static final KafkaTestContainer KAFKA = KafkaTestContainer.instance();
    protected static final RedisTestContainer REDIS = RedisTestContainer.instance();
    protected static final ElasticsearchTestContainer ELASTICSEARCH =
        ElasticsearchTestContainer.instance();

    @BeforeAll
    static void startInfrastructure() {
        Assumptions.assumeTrue(isDockerAvailable(), DOCKER_UNAVAILABLE_MESSAGE);
        Startables.deepStart(
            POSTGRES.container(),
            KAFKA.container(),
            REDIS.container(),
            ELASTICSEARCH.container()
        ).join();
    }

    protected static ServiceRouting requireServiceRouting() {
        ServiceRouting routing = ServiceRouting.fromEnvironment();
        Assumptions.assumeTrue(routing.isConfigured(),
            "E2E service routing is not configured");
        return routing;
    }

    private static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable ex) {
            return false;
        }
    }

    protected record ServiceRouting(
        String gatewayBaseUrl,
        String catalogGrpcTarget,
        String pricingGrpcTarget,
        String cartGrpcTarget,
        String searchGrpcTarget
    ) {
        private static final String GATEWAY_BASE_URL_ENV = "E2E_GATEWAY_BASE_URL";
        private static final String CATALOG_GRPC_TARGET_ENV = "E2E_CATALOG_GRPC_TARGET";
        private static final String PRICING_GRPC_TARGET_ENV = "E2E_PRICING_GRPC_TARGET";
        private static final String CART_GRPC_TARGET_ENV = "E2E_CART_GRPC_TARGET";
        private static final String SEARCH_GRPC_TARGET_ENV = "E2E_SEARCH_GRPC_TARGET";

        static ServiceRouting fromEnvironment() {
            return new ServiceRouting(
                env(GATEWAY_BASE_URL_ENV),
                env(CATALOG_GRPC_TARGET_ENV),
                env(PRICING_GRPC_TARGET_ENV),
                env(CART_GRPC_TARGET_ENV),
                env(SEARCH_GRPC_TARGET_ENV)
            );
        }

        boolean isConfigured() {
            return isPresent(gatewayBaseUrl)
                && isPresent(catalogGrpcTarget)
                && isPresent(pricingGrpcTarget)
                && isPresent(cartGrpcTarget)
                && isPresent(searchGrpcTarget);
        }

        private static String env(String key) {
            return System.getenv(key);
        }

        private static boolean isPresent(String value) {
            return value != null && !value.isBlank();
        }
    }
}
