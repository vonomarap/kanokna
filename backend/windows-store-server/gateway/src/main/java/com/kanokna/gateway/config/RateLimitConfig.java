/* <FUNCTION_CONTRACT
id="FC-gateway-config-RateLimitConfig-rateLimiter"
     LAYER="config"
     INTENT="Apply rate limiting to protect backend services from abuse"
     INPUT="KeyResolver (identifies client for rate limiting)"
     OUTPUT="RedisRateLimiter or in-memory fallback"
     SIDE_EFFECTS="Redis state updated with request counts"
     LINKS="RequirementsAnalysis.xml#NFR-PERF-THROUGHPUT">
  <PRECONDITIONS>
    <Item>Redis available for distributed rate limiting (fallback to in-memory if not)</Item>
  </PRECONDITIONS>

  <POSTCONDITIONS>
    <Item>Requests within limit proceed normally</Item>
    <Item>Requests exceeding limit receive 429 Too Many Requests</Item>
    <Item>Rate limit headers included in response</Item>
  </POSTCONDITIONS>

  <INVARIANTS>
    <Item>Authenticated users: 100 requests per minute</Item>
    <Item>Anonymous users: 20 requests per minute</Item>
    <Item>Rate limit based on client identifier (userId or IP)</Item>
  </INVARIANTS>

  <ERROR_HANDLING>
    <Item type="BUSINESS" code="429">Rate limit exceeded - Retry-After header included</Item>
  </ERROR_HANDLING>

  <BLOCK_ANCHORS>
    <Item id="BA-GW-RATE-01">Resolve client identifier (userId or IP)</Item>
    <Item id="BA-GW-RATE-02">Check rate limit</Item>
    <Item id="BA-GW-RATE-03">Update rate limit counter</Item>
  </BLOCK_ANCHORS>

  <LOGGING>
    <Item>[SVC=gateway][UC=RATE-LIMIT][BLOCK=BA-GW-RATE-02][STATE=LIMIT_CHECK] eventType=RATE_LIMIT decision=ALLOW|REJECT keyValues=clientId,remaining,limit,window</Item>
  </LOGGING>

  <RESPONSE_HEADERS>
    <Header name="X-RateLimit-Limit">Maximum requests per window</Header>
    <Header name="X-RateLimit-Remaining">Requests remaining in window</Header>
    <Header name="X-RateLimit-Reset">Seconds until window resets</Header>
    <Header name="Retry-After">Seconds to wait before retry (on 429)</Header>
  </RESPONSE_HEADERS>

  <TESTS>
    <Case id="TC-RATE-001">Request within limit succeeds with rate limit headers</Case>
    <Case id="TC-RATE-002">Request exceeding limit returns 429</Case>
    <Case id="TC-RATE-003">Authenticated user has higher limit than anonymous</Case>
    <Case id="TC-RATE-004">Rate limit resets after window expires</Case>
    <Case id="TC-RATE-005">Redis unavailable falls back to in-memory limiter</Case>
  </TESTS>
</FUNCTION_CONTRACT> */
package com.kanokna.gateway.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.ratelimit.AbstractRateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;

@Configuration
public class RateLimitConfig {
    private static final Logger logger = LoggerFactory.getLogger(RateLimitConfig.class);

    @Bean
    public KeyResolver keyResolver() {
        return exchange -> exchange.getPrincipal()
            .cast(Authentication.class)
            .map(auth -> "auth:" + auth.getName())
            .switchIfEmpty(Mono.defer(() -> Mono.just("anon:" + resolveClientIp(exchange))));
    }

    @Bean
    public RateLimiter<?> rateLimiter(ObjectProvider<ReactiveStringRedisTemplate> redisTemplateProvider) {
        return new GatewayRateLimiter(redisTemplateProvider.getIfAvailable());
    }

    private static String resolveClientIp(ServerWebExchange exchange) {
        // <BLOCK_ANCHOR id="BA-GW-RATE-01">Resolve client identifier (userId or IP)</BLOCK_ANCHOR>
        ServerHttpRequest request = exchange.getRequest();
        String forwarded = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String[] parts = forwarded.split(",");
            if (parts.length > 0 && !parts[0].isBlank()) {
                return parts[0].trim();
            }
        }
        return Objects.toString(request.getRemoteAddress(), "unknown");
    }

    static final class GatewayRateLimiter extends AbstractRateLimiter<GatewayRateLimiter.Config> {
        private static final String AUTH_PREFIX = "auth:";
        private static final String ANON_PREFIX = "anon:";
        private static final String REDIS_PREFIX = "gateway:rate:";
        private static final int AUTH_LIMIT = 100;
        private static final int ANON_LIMIT = 20;
        private static final long WINDOW_SECONDS = 60L;
        private static final Duration WINDOW = Duration.ofSeconds(WINDOW_SECONDS);

        private final ReactiveStringRedisTemplate redisTemplate;
        private final LongSupplier timeSupplier;
        private final Map<String, Window> inMemory = new ConcurrentHashMap<>();

        GatewayRateLimiter(ReactiveStringRedisTemplate redisTemplate) {
            this(redisTemplate, () -> java.time.Instant.now().getEpochSecond());
        }

        GatewayRateLimiter(ReactiveStringRedisTemplate redisTemplate, LongSupplier timeSupplier) {
            super(Config.class, "gateway-rate-limiter", null);
            this.redisTemplate = redisTemplate;
            this.timeSupplier = timeSupplier;
        }

        @Override
        public Mono<Response> isAllowed(String routeId, String id) {
            RateLimitKey key = RateLimitKey.from(id);
            if (redisTemplate == null) {
                return Mono.fromSupplier(() -> inMemoryDecision(key));
            }
            return redisDecision(key)
                .onErrorResume(ex -> {
                    logger.warn("Redis unavailable for rate limiting, falling back to in-memory", ex);
                    return Mono.fromSupplier(() -> inMemoryDecision(key));
                });
        }

        private Mono<Response> redisDecision(RateLimitKey key) {
            String redisKey = REDIS_PREFIX + key.storageKey();
            return redisTemplate.opsForValue().increment(redisKey)
                .flatMap(count -> {
                    Mono<Boolean> expireMono = count == 1L
                        ? redisTemplate.expire(redisKey, WINDOW)
                        : Mono.just(Boolean.TRUE);
                    return expireMono.thenReturn(count);
                })
                .flatMap(count -> redisTemplate.getExpire(redisKey)
                    .map(Duration::getSeconds)
                    .defaultIfEmpty(WINDOW_SECONDS)
                    .map(ttl -> buildResponse(key, count, ttl))
                );
        }

        private Response inMemoryDecision(RateLimitKey key) {
            long now = timeSupplier.getAsLong();
            Window window = inMemory.computeIfAbsent(key.storageKey(), k -> new Window(now));
            WindowDecision decision = window.increment(now, key.limit());
            return buildResponse(key, decision.count(), decision.resetSeconds());
        }

        private Response buildResponse(RateLimitKey key, long count, long resetSeconds) {
            boolean allowed = count <= key.limit();
            long remaining = Math.max(0L, key.limit() - count);
            long reset = resetSeconds <= 0 ? WINDOW_SECONDS : resetSeconds;

            // <BLOCK_ANCHOR id="BA-GW-RATE-02">Check rate limit</BLOCK_ANCHOR>
            String decision = allowed ? "ALLOW" : "REJECT";
            logger.info(
                "[SVC=gateway][UC=RATE-LIMIT][BLOCK=BA-GW-RATE-02][STATE=LIMIT_CHECK] " +
                "eventType=RATE_LIMIT decision={} keyValues=clientId={},remaining={},limit={},window={}",
                decision,
                key.clientId(),
                remaining,
                key.limit(),
                WINDOW_SECONDS
            );

            // <BLOCK_ANCHOR id="BA-GW-RATE-03">Update rate limit counter</BLOCK_ANCHOR>
            Map<String, String> headers = new HashMap<>();
            headers.put("X-RateLimit-Limit", String.valueOf(key.limit()));
            headers.put("X-RateLimit-Remaining", String.valueOf(remaining));
            headers.put("X-RateLimit-Reset", String.valueOf(reset));
            if (!allowed) {
                headers.put("Retry-After", String.valueOf(reset));
            }

            return new Response(allowed, headers);
        }

        static final class Config {
        }

        private record RateLimitKey(String storageKey, String clientId, int limit) {
            static RateLimitKey from(String raw) {
                if (raw != null && raw.startsWith(AUTH_PREFIX)) {
                    String client = raw.substring(AUTH_PREFIX.length());
                    return new RateLimitKey(raw, client, AUTH_LIMIT);
                }
                if (raw != null && raw.startsWith(ANON_PREFIX)) {
                    String client = raw.substring(ANON_PREFIX.length());
                    return new RateLimitKey(raw, client, ANON_LIMIT);
                }
                String client = raw == null ? "unknown" : raw;
                return new RateLimitKey(raw == null ? "anon:unknown" : raw, client, ANON_LIMIT);
            }
        }

        private static final class Window {
            private long windowStart;
            private long count;

            private Window(long windowStart) {
                this.windowStart = windowStart;
                this.count = 0L;
            }

            private synchronized WindowDecision increment(long now, int limit) {
                if ((now - windowStart) >= WINDOW_SECONDS) {
                    windowStart = now;
                    count = 0L;
                }
                count++;
                long reset = WINDOW_SECONDS - (now - windowStart);
                long remaining = Math.max(0L, limit - count);
                return new WindowDecision(count, remaining, reset);
            }
        }

        private record WindowDecision(long count, long remaining, long resetSeconds) {
        }
    }
}
