package com.kanokna.cart.adapters.config;

import com.kanokna.catalog.v1.CatalogConfigurationServiceGrpc;
import com.kanokna.catalog.v1.CatalogConfigurationServiceGrpc.CatalogConfigurationServiceBlockingStub;
import com.kanokna.pricing.v1.PricingServiceGrpc;
import com.kanokna.pricing.v1.PricingServiceGrpc.PricingServiceBlockingStub;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

/**
 * gRPC client channel configuration.
 */
@Configuration
public class GrpcClientConfig {
    @Bean
    public CatalogConfigurationServiceBlockingStub catalogConfigurationServiceBlockingStub(
        GrpcChannelFactory channels
    ) {
        return CatalogConfigurationServiceGrpc.newBlockingStub(
            channels.createChannel("catalog-configuration-service")
        );
    }

    @Bean
    public PricingServiceBlockingStub pricingServiceBlockingStub(
        GrpcChannelFactory channels
    ) {
        return PricingServiceGrpc.newBlockingStub(
            channels.createChannel("pricing-service")
        );
    }
}
