package com.kanokna.test.containers.redis;

import com.kanokna.test.containers.AbstractTestContainer;
import java.util.Objects;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Redis Testcontainers configuration for cache integration tests.
 */
public final class RedisTestContainer extends AbstractTestContainer<GenericContainer<?>> {
    private static final DockerImageName REDIS_IMAGE =
        DockerImageName.parse("redis:7-alpine");
    private static final int REDIS_PORT = 6379;
    private static final String SPRING_REDIS_HOST = "spring.redis.host";
    private static final String SPRING_REDIS_PORT = "spring.redis.port";

    private static final RedisTestContainer INSTANCE = new RedisTestContainer();

    private RedisTestContainer() {
        super(new GenericContainer<>(REDIS_IMAGE)
            .withExposedPorts(REDIS_PORT)
            .withReuse(true));
    }

    public static RedisTestContainer instance() {
        return INSTANCE;
    }

    public String host() {
        startIfNeeded();
        return withContainer(GenericContainer::getHost);
    }

    public int port() {
        startIfNeeded();
        return withContainer(container -> container.getMappedPort(REDIS_PORT));
    }

    public void registerProperties(DynamicPropertyRegistry registry) {
        Objects.requireNonNull(registry, "registry");
        registry.add(SPRING_REDIS_HOST, this::host);
        registry.add(SPRING_REDIS_PORT, this::port);
    }
}
