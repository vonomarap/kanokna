package com.kanokna.order_service.adapters.out.redis;

import com.kanokna.order_service.application.port.out.IdempotencyStore;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisIdempotencyStore implements IdempotencyStore {

    private final RedisTemplate<String, String> redisTemplate;
    private static final Duration TTL = Duration.ofHours(6);

    public RedisIdempotencyStore(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void put(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        redisTemplate.opsForValue().setIfAbsent(key, "1", TTL);
    }
}
