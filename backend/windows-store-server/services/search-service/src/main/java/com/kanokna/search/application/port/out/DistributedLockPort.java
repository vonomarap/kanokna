package com.kanokna.search.application.port.out;

/**
 * Outbound port for distributed lock acquisition.
 */
public interface DistributedLockPort {
    LockHandle tryAcquire(String lockName);

    interface LockHandle extends AutoCloseable {
        void release();

        @Override
        default void close() {
            release();
        }
    }
}
