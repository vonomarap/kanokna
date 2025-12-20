package com.kanokna.pricing_service.application.port.out;

import com.kanokna.pricing_service.domain.model.TaxRule;

import java.util.Optional;

public interface TaxRuleRepository {

    Optional<TaxRule> findForRegion(String region);
}
