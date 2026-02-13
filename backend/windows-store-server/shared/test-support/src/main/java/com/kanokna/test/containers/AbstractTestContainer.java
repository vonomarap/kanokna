package com.kanokna.test.containers;

import java.util.Objects;
import org.testcontainers.containers.GenericContainer;

/**
 * Base class for shared singleton Testcontainers wrappers.
 *
 * @param <T> concrete Testcontainer type
 */
public abstract class AbstractTestContainer<T extends GenericContainer<?>> {
    private final T container;

    protected AbstractTestContainer(T container) {
        this.container = Objects.requireNonNull(container, "container");
    }

    public final T container() {
        return container;
    }

    public final void startIfNeeded() {
        if (!container.isRunning()) {
            container.start();
        }
    }
}
