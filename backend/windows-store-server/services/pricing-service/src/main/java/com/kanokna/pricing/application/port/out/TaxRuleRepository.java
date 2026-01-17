package com.kanokna.pricing.application.port.out;

import com.kanokna.pricing.domain.model.TaxRule;
import java.util.Optional;

/**
 * Outbound port for tax rule persistence.
 */
public interface TaxRuleRepository {
    Optional<TaxRule> findByRegion(String region);

    TaxRule save(TaxRule taxRule);
}
