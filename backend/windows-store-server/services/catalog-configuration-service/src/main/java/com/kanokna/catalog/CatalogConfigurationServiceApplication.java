package com.kanokna.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for catalog-configuration-service.
 * HTTP server: 8081
 * gRPC server: 9081
 */
@SpringBootApplication
public class CatalogConfigurationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogConfigurationServiceApplication.class, args);
    }
}
