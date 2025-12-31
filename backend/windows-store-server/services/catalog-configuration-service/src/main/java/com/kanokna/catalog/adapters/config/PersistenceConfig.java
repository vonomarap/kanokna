package com.kanokna.catalog.adapters.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.catalog.domain.service.BomResolutionService;
import com.kanokna.catalog.domain.service.ConfigurationValidationService;
import com.kanokna.catalog.domain.service.RuleEvaluator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA and transaction configuration.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.kanokna.catalog.adapters.out.persistence")
@EnableTransactionManagement
public class PersistenceConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public RuleEvaluator ruleEvaluator() {
        return new RuleEvaluator();
    }

    @Bean
    public ConfigurationValidationService configurationValidationService(RuleEvaluator ruleEvaluator) {
        return new ConfigurationValidationService(ruleEvaluator);
    }

    @Bean
    public BomResolutionService bomResolutionService() {
        return new BomResolutionService();
    }
}
