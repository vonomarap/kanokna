package com.kanokna.cart.adapters.config;

import com.kanokna.cart.domain.service.CartMergeService;
import com.kanokna.cart.domain.service.CartTotalsCalculator;
import com.kanokna.cart.domain.service.ConfigurationHashService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cart service configuration and core bean wiring.
 */
@Configuration
@EnableConfigurationProperties(CartProperties.class)
public class CartServiceConfig {
    @Bean
    public CartTotalsCalculator cartTotalsCalculator() {
        return new CartTotalsCalculator();
    }

    @Bean
    public ConfigurationHashService configurationHashService() {
        return new ConfigurationHashService();
    }

    @Bean
    public CartMergeService cartMergeService() {
        return new CartMergeService();
    }
}
