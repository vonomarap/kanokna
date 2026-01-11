package com.kanokna.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for search-service.
 * HTTP server: 8089
 * gRPC server: 9089
 */
@SpringBootApplication
public class SearchServiceApplication {
    static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}
