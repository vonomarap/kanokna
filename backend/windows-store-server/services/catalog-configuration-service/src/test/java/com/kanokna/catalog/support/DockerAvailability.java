package com.kanokna.catalog.support;

import org.testcontainers.DockerClientFactory;

/**
 * Utility class to check Docker availability for Testcontainers.
 */
public final class DockerAvailability {

    private DockerAvailability() {
    }

    /**
     * Checks if Docker is available for running Testcontainers.
     *
     * @return true if Docker is available, false otherwise
     */
    public static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable ex) {
            return false;
        }
    }
}
