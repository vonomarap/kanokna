package com.kanokna.account.adapters.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kanokna.account.application.port.out.EventPublisher;

/**
 * Account service configuration and adapter wiring.
 */
@Configuration
@EnableConfigurationProperties(AccountProperties.class)
public class AccountServiceConfig {
    private static final Logger log = LoggerFactory.getLogger(AccountServiceConfig.class);

    @Bean
    public EventPublisher eventPublisher() {
        return new EventPublisher() {
            @Override
            public <T> void publish(String topic, T event) {
                log.info("account-service event published topic={} eventType={}", topic, event.getClass().getSimpleName());
            }
        };
    }
}
