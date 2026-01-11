package com.kanokna.pricing_service.application.port.out;

import com.kanokna.pricing_service.domain.model.TaxRule;
import java.util.Optional;

/**
 * Outbound port for tax rule persistence.
 */
public interface TaxRuleRepository {
    Optional<TaxRule> findByRegion(String region);

    TaxRule save(TaxRule taxRule);
}
