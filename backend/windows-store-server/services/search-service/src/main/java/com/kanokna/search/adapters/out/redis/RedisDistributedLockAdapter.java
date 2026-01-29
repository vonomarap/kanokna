package com.kanokna.search.adapters.out.redis;

import com.kanokna.search.application.port.out.DistributedLockPort;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 * Redisson-backed distributed lock adapter.
 */
@Component
public class RedisDistributedLockAdapter implements DistributedLockPort {
    private final RedissonClient redissonClient;

    public RedisDistributedLockAdapter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public LockHandle tryAcquire(String lockName) {
        if (lockName == null || lockName.isBlank()) {
            throw new IllegalArgumentException("lockName is required");
        }
        RLock lock = redissonClient.getLock(lockName);
        // Reindex requests must be rejected while a reindex is in progress.
        // Redisson locks are re-entrant for the same thread, but for our use-case
        // re-entrancy would mask contention in tests and in-process callers.
        if (lock.isHeldByCurrentThread()) {
            return null;
        }
        boolean acquired = lock.tryLock();
        if (!acquired) {
            return null;
        }
        return new RedissonLockHandle(lock);
    }

    private record RedissonLockHandle(RLock lock) implements LockHandle {

        @Override
            public void release() {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
}
