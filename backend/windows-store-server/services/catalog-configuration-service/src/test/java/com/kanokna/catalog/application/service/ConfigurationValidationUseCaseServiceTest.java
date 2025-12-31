package com.kanokna.catalog.application.service;

import com.kanokna.catalog.application.dto.ConfigurationResponse;
import com.kanokna.catalog.application.dto.ValidateConfigurationCommand;
import com.kanokna.catalog.application.port.out.ConfigurationRuleSetRepository;
import com.kanokna.catalog.application.port.out.PricingClient;
import com.kanokna.catalog.application.port.out.ProductTemplateRepository;
import com.kanokna.catalog.domain.model.*;
import com.kanokna.catalog.domain.service.ConfigurationValidationService;
import com.kanokna.catalog.domain.service.RuleEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ConfigurationValidationUseCaseService.
 */
@ExtendWith(MockitoExtension.class)
class ConfigurationValidationUseCaseServiceTest {

    @Mock
    private ProductTemplateRepository productTemplateRepository;

    @Mock
    private ConfigurationRuleSetRepository ruleSetRepository;

    @Mock
    private PricingClient pricingClient;

    private ConfigurationValidationUseCaseService validationUseCaseService;

    @BeforeEach
    void setUp() {
        RuleEvaluator ruleEvaluator = new RuleEvaluator();
        ConfigurationValidationService validationService = new ConfigurationValidationService(ruleEvaluator);

        validationUseCaseService = new ConfigurationValidationUseCaseService(
            productTemplateRepository,
            ruleSetRepository,
            validationService,
            pricingClient
        );
    }

    @Test
    @DisplayName("Valid configuration returns success with price")
    void validConfiguration_ReturnsSuccessWithPrice() {
        // Given
        ProductTemplateId id = ProductTemplateId.generate();
        ProductTemplate template = ProductTemplate.create(
            "Test Window",
            "Description",
            ProductFamily.WINDOW,
            DimensionConstraints.standard()
        );

        ValidateConfigurationCommand command = new ValidateConfigurationCommand(
            id.value(),
            120,
            150,
            Map.of()
        );

        when(productTemplateRepository.findById(id)).thenReturn(Optional.of(template));
        when(ruleSetRepository.findActiveByProductTemplateId(id)).thenReturn(Optional.empty());
        when(pricingClient.getQuote(any(), any())).thenReturn(BigDecimal.valueOf(1000));

        // When
        ConfigurationResponse response = validationUseCaseService.validate(command);

        // Then
        assertTrue(response.valid());
        assertEquals(BigDecimal.valueOf(1000), response.priceQuote());
    }

    @Test
    @DisplayName("Invalid configuration returns errors without price")
    void invalidConfiguration_ReturnsErrorsWithoutPrice() {
        // Given
        ProductTemplateId id = ProductTemplateId.generate();
        ProductTemplate template = ProductTemplate.create(
            "Test Window",
            "Description",
            ProductFamily.WINDOW,
            DimensionConstraints.standard()
        );

        ValidateConfigurationCommand command = new ValidateConfigurationCommand(
            id.value(),
            30, // Below minimum
            150,
            Map.of()
        );

        when(productTemplateRepository.findById(id)).thenReturn(Optional.of(template));
        when(ruleSetRepository.findActiveByProductTemplateId(id)).thenReturn(Optional.empty());

        // When
        ConfigurationResponse response = validationUseCaseService.validate(command);

        // Then
        assertFalse(response.valid());
        assertNull(response.priceQuote());
        assertFalse(response.errors().isEmpty());
    }
}
