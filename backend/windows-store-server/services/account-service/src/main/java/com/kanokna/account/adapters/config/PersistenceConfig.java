package com.kanokna.account.adapters.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA and transaction configuration.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.kanokna.account.adapters.out.persistence")
@EnableTransactionManagement
public class PersistenceConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
