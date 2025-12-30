package com.kanokna.gateway.config;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RateLimitConfigTest {
    @Test
    void requestWithinLimitSucceedsWithHeaders() {
        AtomicLong now = new AtomicLong(0L);
        RateLimitConfig.GatewayRateLimiter limiter = new RateLimitConfig.GatewayRateLimiter(null, now::get);

        RateLimiter.Response response = limiter.isAllowed("route", "anon:client-1").block();

        assertThat(response).isNotNull();
        assertThat(response.isAllowed()).isTrue();
        assertThat(response.getHeaders().get("X-RateLimit-Limit")).isEqualTo("20");
        assertThat(response.getHeaders().get("X-RateLimit-Remaining")).isEqualTo("19");
        assertThat(response.getHeaders().get("X-RateLimit-Reset")).isNotBlank();
    }

    @Test
    void requestExceedingLimitReturnsRejected() {
        AtomicLong now = new AtomicLong(0L);
        RateLimitConfig.GatewayRateLimiter limiter = new RateLimitConfig.GatewayRateLimiter(null, now::get);

        RateLimiter.Response last = null;
        for (int i = 0; i < 21; i++) {
            last = limiter.isAllowed("route", "anon:client-2").block();
        }

        assertThat(last).isNotNull();
        assertThat(last.isAllowed()).isFalse();
        assertThat(last.getHeaders().get("Retry-After")).isNotBlank();
    }

    @Test
    void authenticatedUsersHaveHigherLimit() {
        AtomicLong now = new AtomicLong(0L);
        RateLimitConfig.GatewayRateLimiter limiter = new RateLimitConfig.GatewayRateLimiter(null, now::get);

        RateLimiter.Response anon = limiter.isAllowed("route", "anon:client-3").block();
        RateLimiter.Response auth = limiter.isAllowed("route", "auth:user-1").block();

        assertThat(anon).isNotNull();
        assertThat(auth).isNotNull();
        assertThat(anon.getHeaders().get("X-RateLimit-Limit")).isEqualTo("20");
        assertThat(auth.getHeaders().get("X-RateLimit-Limit")).isEqualTo("100");
    }

    @Test
    void rateLimitResetsAfterWindowExpires() {
        AtomicLong now = new AtomicLong(0L);
        RateLimitConfig.GatewayRateLimiter limiter = new RateLimitConfig.GatewayRateLimiter(null, now::get);

        for (int i = 0; i < 21; i++) {
            limiter.isAllowed("route", "anon:client-4").block();
        }

        now.addAndGet(61L);
        RateLimiter.Response response = limiter.isAllowed("route", "anon:client-4").block();

        assertThat(response).isNotNull();
        assertThat(response.isAllowed()).isTrue();
        assertThat(response.getHeaders().get("X-RateLimit-Remaining")).isEqualTo("19");
    }

    @Test
    void redisUnavailableFallsBackToInMemory() {
        AtomicLong now = new AtomicLong(0L);
        ReactiveStringRedisTemplate redisTemplate = mock(ReactiveStringRedisTemplate.class);
        ReactiveValueOperations<String, String> valueOps = mock(ReactiveValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(anyString())).thenReturn(Mono.error(new RuntimeException("redis down")));

        RateLimitConfig.GatewayRateLimiter limiter = new RateLimitConfig.GatewayRateLimiter(redisTemplate, now::get);

        RateLimiter.Response response = limiter.isAllowed("route", "anon:client-5").block();

        assertThat(response).isNotNull();
        assertThat(response.isAllowed()).isTrue();
    }
}
