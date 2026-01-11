package com.kanokna.catalog.adapters.out.persistence;

import com.kanokna.catalog.application.port.out.ConfigurationRuleSetRepository;
import com.kanokna.catalog.domain.model.ConfigurationRuleSet;
import com.kanokna.catalog.domain.model.ProductTemplateId;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing ConfigurationRuleSetRepository port.
 * Simplified stub implementation.
 */
@Component
public class ConfigurationRuleSetRepositoryAdapter implements ConfigurationRuleSetRepository {

    @Override
    public ConfigurationRuleSet save(ConfigurationRuleSet ruleSet) {
        // Stub: would persist to configuration_rule_sets table
        return ruleSet;
    }

    @Override
    public Optional<ConfigurationRuleSet> findById(UUID id) {
        // Stub: would query from database
        return Optional.empty();
    }

    @Override
    public Optional<ConfigurationRuleSet> findActiveByProductTemplateId(ProductTemplateId productTemplateId) {
        // Stub: would query active rule set for product
        return Optional.empty();
    }
}
