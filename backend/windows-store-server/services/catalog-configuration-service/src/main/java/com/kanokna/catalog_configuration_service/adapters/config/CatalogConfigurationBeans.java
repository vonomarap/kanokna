package com.kanokna.catalog_configuration_service.adapters.config;

import com.kanokna.catalog_configuration_service.adapters.out.memory.InMemoryBomTemplateRepository;
import com.kanokna.catalog_configuration_service.adapters.out.memory.InMemoryProductTemplateRepository;
import com.kanokna.catalog_configuration_service.application.port.out.BomTemplateRepository;
import com.kanokna.catalog_configuration_service.application.port.out.OutboxPublisher;
import com.kanokna.catalog_configuration_service.application.port.out.ProductTemplateRepository;
import com.kanokna.catalog_configuration_service.application.service.ConfigurationApplicationService;
import com.kanokna.catalog_configuration_service.domain.service.BomResolutionService;
import com.kanokna.catalog_configuration_service.domain.service.ConfigurationValidationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogConfigurationBeans {

    @Bean
    public ConfigurationValidationService configurationValidationService() {
        return new ConfigurationValidationService();
    }

    @Bean
    public BomResolutionService bomResolutionService() {
        return new BomResolutionService();
    }

    @Bean
    public ConfigurationApplicationService configurationApplicationService(
        ProductTemplateRepository productTemplateRepository,
        BomTemplateRepository bomTemplateRepository,
        OutboxPublisher outboxPublisher,
        ConfigurationValidationService validationService,
        BomResolutionService bomResolutionService
    ) {
        return new ConfigurationApplicationService(
            productTemplateRepository,
            bomTemplateRepository,
            outboxPublisher,
            validationService,
            bomResolutionService
        );
    }

    @Bean
    public ProductTemplateRepository productTemplateRepository(InMemoryProductTemplateRepository repository) {
        return repository;
    }

    @Bean
    public BomTemplateRepository bomTemplateRepository(InMemoryBomTemplateRepository repository) {
        return repository;
    }
}
