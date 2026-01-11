package com.kanokna.pricing_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Pricing Service.
 *
 * Service ports:
 * - HTTP: 8082
 * - gRPC: 9082
 *
 * Per DevelopmentPlan.xml#DP-SVC-pricing-service
 */
@SpringBootApplication
public class PricingServiceApplication {

    static void main(String[] args) {
        SpringApplication.run(PricingServiceApplication.class, args);
    }
}

