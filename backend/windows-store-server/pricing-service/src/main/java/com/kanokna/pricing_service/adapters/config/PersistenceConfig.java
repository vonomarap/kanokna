package com.kanokna.pricing_service.adapters.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.pricing_service.domain.service.DiscountService;
import com.kanokna.pricing_service.domain.service.PriceCalculationService;
import com.kanokna.pricing_service.domain.service.RoundingService;
import com.kanokna.pricing_service.domain.service.TaxCalculationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA and domain service configuration.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.kanokna.pricing_service.adapters.out.persistence")
@EnableTransactionManagement
public class PersistenceConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    public DiscountService discountService() {
        return new DiscountService();
    }

    @Bean
    public TaxCalculationService taxCalculationService() {
        return new TaxCalculationService();
    }

    @Bean
    public RoundingService roundingService() {
        return new RoundingService();
    }

    @Bean
    public PriceCalculationService priceCalculationService(
            DiscountService discountService,
            TaxCalculationService taxCalculationService,
            RoundingService roundingService) {
        return new PriceCalculationService(discountService, taxCalculationService, roundingService);
    }
}

