package com.kanokna.pricing_service.adapters.config;

import com.kanokna.pricing_service.adapters.out.memory.InMemoryQuoteCache;
import com.kanokna.pricing_service.adapters.out.persistence.CampaignJpaAdapter;
import com.kanokna.pricing_service.adapters.out.persistence.PriceBookJpaAdapter;
import com.kanokna.pricing_service.adapters.out.persistence.TaxRuleJpaAdapter;
import com.kanokna.pricing_service.adapters.out.redis.RedisQuoteCacheAdapter;
import com.kanokna.pricing_service.application.port.out.CampaignRepository;
import com.kanokna.pricing_service.application.port.out.OutboxPublisher;
import com.kanokna.pricing_service.application.port.out.PriceBookRepository;
import com.kanokna.pricing_service.application.port.out.QuoteCache;
import com.kanokna.pricing_service.application.port.out.TaxRuleRepository;
import com.kanokna.pricing_service.application.service.PriceApplicationService;
import com.kanokna.pricing_service.domain.service.PriceCalculationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class PricingServiceConfig {

    @Bean
    public PriceCalculationService priceCalculationService() {
        return new PriceCalculationService();
    }

    @Bean
    public PriceApplicationService priceApplicationService(
        PriceBookRepository priceBookRepository,
        CampaignRepository campaignRepository,
        TaxRuleRepository taxRuleRepository,
        QuoteCache quoteCache,
        OutboxPublisher outboxPublisher,
        PriceCalculationService priceCalculationService
    ) {
        return new PriceApplicationService(
            priceBookRepository,
            campaignRepository,
            taxRuleRepository,
            quoteCache,
            outboxPublisher,
            priceCalculationService
        );
    }

    @Bean
    public PriceBookRepository priceBookRepository(PriceBookJpaAdapter repo) {
        return repo;
    }

    @Bean
    public CampaignRepository campaignRepository(CampaignJpaAdapter repo) {
        return repo;
    }

    @Bean
    public TaxRuleRepository taxRuleRepository(TaxRuleJpaAdapter repo) {
        return repo;
    }

    @Bean
    public QuoteCache quoteCache(InMemoryQuoteCache cache) {
        return cache;
    }

    @Bean
    @Primary
    public QuoteCache redisQuoteCache(RedisQuoteCacheAdapter adapter) {
        return adapter;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}
