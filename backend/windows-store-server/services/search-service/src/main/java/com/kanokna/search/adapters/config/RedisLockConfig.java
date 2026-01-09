package com.kanokna.search.adapters.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson configuration for distributed locks.
 */
@Configuration
public class RedisLockConfig {
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(
        @Value("${spring.redis.host}") String host,
        @Value("${spring.redis.port}") int port
    ) {
        Config config = new Config();
        config.setLockWatchdogTimeout(30_000L);
        config.useSingleServer().setAddress("redis://" + host + ":" + port);
        return Redisson.create(config);
    }
}
