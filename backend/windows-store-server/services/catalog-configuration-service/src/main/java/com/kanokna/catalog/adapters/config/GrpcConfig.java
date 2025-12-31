package com.kanokna.catalog.adapters.config;

import org.springframework.context.annotation.Configuration;

/**
 * gRPC server and client configuration.
 * Server runs on port 9081 (configured in application.yml).
 * Client to pricing-service on port 9082.
 */
@Configuration
public class GrpcConfig {
    // gRPC configuration via grpc-spring-boot-starter
    // Server and client beans configured automatically from application.yml
}
