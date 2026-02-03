package com.kanokna.catalog.application.service;

import com.kanokna.catalog.application.dto.ConfigurationResponse;
import com.kanokna.catalog.application.dto.ValidateConfigurationCommand;
import com.kanokna.catalog.application.port.in.ValidateConfigurationUseCase;
import com.kanokna.catalog.application.port.out.BomTemplateRepository;
import com.kanokna.catalog.application.port.out.ConfigurationRuleSetRepository;
import com.kanokna.catalog.application.port.out.PricingClient;
import com.kanokna.catalog.application.port.out.ProductTemplateRepository;
import com.kanokna.catalog.domain.exception.ProductTemplateNotFoundException;
import com.kanokna.catalog.domain.model.*;
import com.kanokna.catalog.domain.service.ConfigurationValidationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * MODULE_CONTRACT id="MC-catalog-validation-service"
 * LAYER="application.service" INTENT="Configuration validation with rule engine
 * and optional pricing quote"
 * LINKS="RequirementsAnalysis.xml#UC-CATALOG-CONFIGURE-PRODUCT;Technology.xml#DEC-VALIDATION-ENGINE"
 *
 * Application service implementing configuration validation use case.
 * Orchestrates domain validation + optional pricing call.
 */
@Service
@Transactional(readOnly = true)
public class ConfigurationValidationUseCaseService implements ValidateConfigurationUseCase {

    private final ProductTemplateRepository productTemplateRepository;
    private final ConfigurationRuleSetRepository ruleSetRepository;
    private final ConfigurationValidationService validationService;
    private final PricingClient pricingClient;

    public ConfigurationValidationUseCaseService(
            ProductTemplateRepository productTemplateRepository,
            ConfigurationRuleSetRepository ruleSetRepository,
            ConfigurationValidationService validationService,
            PricingClient pricingClient
    ) {
        this.productTemplateRepository = productTemplateRepository;
        this.ruleSetRepository = ruleSetRepository;
        this.validationService = validationService;
        this.pricingClient = pricingClient;
    }

    @Override
    public ConfigurationResponse validate(ValidateConfigurationCommand command) {
        // Load product template
        ProductTemplateId productTemplateId = ProductTemplateId.of(command.productTemplateId());
        ProductTemplate productTemplate = productTemplateRepository.findById(productTemplateId)
                .orElseThrow(() -> new ProductTemplateNotFoundException(productTemplateId));

        // Build configuration value object
        Configuration configuration = new Configuration(
                command.widthCm(),
                command.heightCm(),
                command.selectedOptions()
        );

        // Load rule set (optional)
        ConfigurationRuleSet ruleSet = ruleSetRepository
                .findActiveByProductTemplateId(productTemplateId)
                .orElse(null);

        // Validate configuration
        ValidationResult validationResult = validationService.validate(configuration, productTemplate, ruleSet);

        // If valid, get price quote
        BigDecimal priceQuote = null;
        if (validationResult.isValid()) {
            try {
                priceQuote = pricingClient.getQuote(productTemplateId, configuration);
            } catch (Exception e) {
                // Pricing failure doesn't fail validation
                // Log and return validation result without price
            }
        }

        // Map to response DTO
        var errorDtos = validationResult.getErrors().stream()
                .map(err -> new ConfigurationResponse.ValidationErrorDto(
                err.code(),
                err.message(),
                err.field()
        ))
                .collect(Collectors.toList());

        return new ConfigurationResponse(validationResult.isValid(), errorDtos, priceQuote);
    }
}
