package com.kanokna.gateway.config;

import java.time.Duration;

import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.client.circuitbreaker.Customizer;

@Configuration
public class CircuitBreakerConfig {
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> gatewayCircuitBreakerCustomizer() {
        io.github.resilience4j.circuitbreaker.CircuitBreakerConfig circuitBreakerConfig = 
            io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
            .slidingWindowSize(10)
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .permittedNumberOfCallsInHalfOpenState(3)
            .slowCallDurationThreshold(Duration.ofSeconds(2))
            .slowCallRateThreshold(50)
            .build();

        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(10))
            .build();

        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            // <BLOCK_ANCHOR id="BA-GW-CB-01">Check circuit breaker state</BLOCK_ANCHOR>
            .circuitBreakerConfig(circuitBreakerConfig)
            // <BLOCK_ANCHOR id="BA-GW-CB-02">Record success/failure</BLOCK_ANCHOR>
            .timeLimiterConfig(timeLimiterConfig)
            .build());
    }
}
