package com.kanokna.pricing_service.adapters.out.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.pricing_service.application.port.out.QuoteCache;
import com.kanokna.pricing_service.domain.model.Quote;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class RedisQuoteCacheAdapter implements QuoteCache {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Duration TTL = Duration.ofMinutes(10);

    public RedisQuoteCacheAdapter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<Quote> get(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, Quote.class));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void put(String key, Quote quote) {
        if (key == null || key.isBlank() || quote == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(quote), TTL);
        } catch (Exception ignored) {
        }
    }
}
