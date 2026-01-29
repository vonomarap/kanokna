package com.kanokna.search.adapters.out.redis;

import com.kanokna.search.application.port.out.DistributedLockPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisDistributedLockAdapterTest {
    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock lock;

    @Test
    @DisplayName("TC-FUNC-REINDEX-001: tryLock_available_returnsTrue")
    void tryLock_available_returnsTrue() {
        when(redissonClient.getLock("search-lock")).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);

        RedisDistributedLockAdapter adapter = new RedisDistributedLockAdapter(redissonClient);

        DistributedLockPort.LockHandle handle = adapter.tryAcquire("search-lock");

        assertNotNull(handle);
        verify(redissonClient).getLock("search-lock");
    }

    @Test
    @DisplayName("TC-FUNC-REINDEX-003: tryLock_alreadyHeld_returnsFalse")
    void tryLock_alreadyHeld_returnsFalse() {
        when(redissonClient.getLock("search-lock")).thenReturn(lock);
        when(lock.tryLock()).thenReturn(false);

        RedisDistributedLockAdapter adapter = new RedisDistributedLockAdapter(redissonClient);

        DistributedLockPort.LockHandle handle = adapter.tryAcquire("search-lock");

        assertNull(handle);
    }

    @Test
    @DisplayName("TC-FUNC-REINDEX-001: unlock_releasesLock")
    void unlock_releasesLock() {
        when(redissonClient.getLock("search-lock")).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        // First call (during acquire): not held yet; second call (during release): held by current thread.
        when(lock.isHeldByCurrentThread()).thenReturn(false, true);

        RedisDistributedLockAdapter adapter = new RedisDistributedLockAdapter(redissonClient);

        DistributedLockPort.LockHandle handle = adapter.tryAcquire("search-lock");
        handle.release();

        verify(lock).unlock();
    }
}
