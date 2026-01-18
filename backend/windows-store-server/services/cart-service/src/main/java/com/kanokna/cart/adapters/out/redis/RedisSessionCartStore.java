package com.kanokna.cart.adapters.out.redis;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.kanokna.cart.adapters.config.CartProperties;
import com.kanokna.cart.application.port.out.SessionCartStore;

/**
 * Redis-backed store for anonymous cart session IDs.
 */
@Component
public class RedisSessionCartStore implements SessionCartStore {
    private static final String KEY_PREFIX = "cart:session:";

    private final StringRedisTemplate redisTemplate;
    private final CartProperties properties;

    public RedisSessionCartStore(StringRedisTemplate redisTemplate, CartProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @Override
    public Optional<String> findCartId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(sessionId)));
    }

    @Override
    public void storeCartId(String sessionId, String cartId) {
        if (sessionId == null || sessionId.isBlank() || cartId == null || cartId.isBlank()) {
            return;
        }
        Duration ttl = properties.timeouts().anonymousTtl();
        redisTemplate.opsForValue().set(key(sessionId), cartId, ttl);
    }

    @Override
    public void removeCartId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        redisTemplate.delete(key(sessionId));
    }

    private String key(String sessionId) {
        return KEY_PREFIX + sessionId;
    }
}
