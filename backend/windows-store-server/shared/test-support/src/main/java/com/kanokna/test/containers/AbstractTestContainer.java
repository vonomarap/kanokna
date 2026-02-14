package com.kanokna.test.containers;

import java.util.Objects;
import java.util.function.Function;
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

    protected final <R> R withContainer(Function<? super T, ? extends R> extractor) {
        Objects.requireNonNull(extractor, "extractor");
        return extractor.apply(container);
    }

    public final void startIfNeeded() {
        if (!container.isRunning()) {
            container.start();
        }
    }
}
