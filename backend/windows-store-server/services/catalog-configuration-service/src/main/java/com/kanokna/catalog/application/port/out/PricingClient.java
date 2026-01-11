package com.kanokna.catalog.application.port.out;

import com.kanokna.catalog.domain.model.Configuration;
import com.kanokna.catalog.domain.model.ProductTemplateId;

import java.math.BigDecimal;

/**
 * Outbound port: Pricing service client (gRPC).
 */
public interface PricingClient {

    /**
     * Request price quote for a validated configuration.
     *
     * @param productTemplateId product template ID
     * @param configuration     validated configuration
     * @return price quote in default currency
     */
    BigDecimal getQuote(ProductTemplateId productTemplateId, Configuration configuration);
}
