package com.kanokna.catalog_configuration_service.adapters.out.memory;

import com.kanokna.catalog_configuration_service.application.port.out.RuleRepository;
import com.kanokna.catalog_configuration_service.domain.model.ConfigurationRuleSet;
import com.kanokna.shared.core.Id;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class NoOpRuleRepository implements RuleRepository {
    @Override
    public Optional<ConfigurationRuleSet> findByTemplate(Id templateId, Id tenantId) {
        return Optional.empty();
    }
}
