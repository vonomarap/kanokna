package com.kanokna.pricing_service.adapters.out.memory;

import com.kanokna.pricing_service.application.port.out.TaxRuleRepository;
import com.kanokna.pricing_service.domain.model.TaxRule;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryTaxRuleRepository implements TaxRuleRepository {

    private final Map<String, TaxRule> rules = new ConcurrentHashMap<>();

    @Override
    public Optional<TaxRule> findForRegion(String region) {
        return Optional.ofNullable(rules.get(region));
    }

    public void save(TaxRule rule) {
        rules.put(rule.region(), rule);
    }
}
