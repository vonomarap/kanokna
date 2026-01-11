package com.kanokna.catalog.adapters.out.grpc;

import com.kanokna.catalog.application.port.out.PricingClient;
import com.kanokna.catalog.domain.model.Configuration;
import com.kanokna.catalog.domain.model.ProductTemplateId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * gRPC client adapter to pricing-service.
 * Stub implementation - would use generated gRPC stubs in production.
 */
@Component
public class PricingGrpcClientAdapter implements PricingClient {

    private static final Logger log = LoggerFactory.getLogger(PricingGrpcClientAdapter.class);

    @Override
    public BigDecimal getQuote(ProductTemplateId productTemplateId, Configuration configuration) {
        log.debug("Requesting price quote for product {} with dimensions {}x{} cm",
            productTemplateId, configuration.widthCm(), configuration.heightCm());

        // Stub: simplified pricing logic
        // In production, would call pricing-service via gRPC
        double area = configuration.getAreaM2();
        double pricePerM2 = 500.0; // Base price
        double totalPrice = area * pricePerM2;

        log.info("Price quote calculated: {} EUR for area {} m2", totalPrice, area);
        return BigDecimal.valueOf(totalPrice);
    }
}
