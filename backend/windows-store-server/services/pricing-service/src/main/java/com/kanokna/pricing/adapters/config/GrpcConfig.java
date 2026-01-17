package com.kanokna.pricing.adapters.config;

import org.springframework.context.annotation.Configuration;

/**
 * gRPC server and client configuration.
 * Server runs on port 9082 (configured in application.yml).
 */
@Configuration
public class GrpcConfig {
    // gRPC configuration via grpc-spring-boot-starter
    // Server and client beans configured automatically from application.yml
}
