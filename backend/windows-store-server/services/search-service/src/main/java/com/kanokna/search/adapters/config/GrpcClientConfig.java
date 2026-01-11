package com.kanokna.search.adapters.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

import com.kanokna.catalog.v1.CatalogConfigurationServiceGrpc;
import com.kanokna.catalog.v1.CatalogConfigurationServiceGrpc.CatalogConfigurationServiceBlockingStub;

/**
 * gRPC client configuration for search-service.
 * Uses Spring gRPC for client channel management.
 * 
 * Client channels are configured via spring.grpc.client.channels.* properties in application.yml.
 * 
 * @see <a href="https://docs.spring.io/spring-grpc/reference/client.html">Spring gRPC Client Reference</a>
 */
@Configuration
public class GrpcClientConfig {

    /**
     * Creates a blocking stub for the CatalogConfigurationService.
     * Uses the named channel "catalog-configuration-service" from application.yml.
     *
     * @param channels GrpcChannelFactory auto-provided by Spring gRPC
     * @return CatalogConfigurationServiceBlockingStub for synchronous gRPC calls
     */
    @Bean
    public CatalogConfigurationServiceBlockingStub catalogConfigurationServiceBlockingStub(
            GrpcChannelFactory channels) {
        return CatalogConfigurationServiceGrpc.newBlockingStub(
            channels.createChannel("catalog-configuration-service")
        );
    }
}

