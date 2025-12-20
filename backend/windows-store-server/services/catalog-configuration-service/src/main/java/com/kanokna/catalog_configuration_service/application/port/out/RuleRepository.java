package com.kanokna.catalog_configuration_service.application.port.out;

import com.kanokna.catalog_configuration_service.domain.model.ConfigurationRuleSet;
import com.kanokna.shared.core.Id;

import java.util.Optional;

public interface RuleRepository {

    Optional<ConfigurationRuleSet> findByTemplate(Id templateId, Id tenantId);
}
