package com.kanokna.account.adapters.config;

import com.kanokna.account.application.port.out.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Account service configuration and adapter wiring.
 */
@Configuration
@EnableConfigurationProperties(AccountProperties.class)
public class AccountServiceConfig {
    private static final Logger log = LoggerFactory.getLogger(AccountServiceConfig.class);

    @Bean
    public EventPublisher eventPublisher() {
        return (topic, event) ->
            log.info("account-service event published topic={} eventType={}", topic, event.getClass().getSimpleName());
    }
}
