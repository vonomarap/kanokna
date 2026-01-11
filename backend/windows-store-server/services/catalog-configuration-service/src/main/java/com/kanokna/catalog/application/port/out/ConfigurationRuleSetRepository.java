package com.kanokna.catalog.application.port.out;

import com.kanokna.catalog.domain.model.ConfigurationRuleSet;
import com.kanokna.catalog.domain.model.ProductTemplateId;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: Configuration rule set persistence.
 */
public interface ConfigurationRuleSetRepository {

    ConfigurationRuleSet save(ConfigurationRuleSet ruleSet);

    Optional<ConfigurationRuleSet> findById(UUID id);

    Optional<ConfigurationRuleSet> findActiveByProductTemplateId(ProductTemplateId productTemplateId);
}
