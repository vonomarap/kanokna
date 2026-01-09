package com.kanokna.search.adapters.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Search service configuration and adapter wiring.
 */
@Configuration
@EnableConfigurationProperties(SearchProperties.class)
public class SearchServiceConfig {
}
