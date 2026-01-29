package com.kanokna.search.adapters.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Jackson ObjectMapper configuration for search-service.
 * Spring Boot 4.0 / Jackson 3.0 compatible configuration.
 * 
 * Note: In Jackson 3.0, Java 8+ date/time support (JSR-310) is built into
 * jackson-databind core, so JavaTimeModule registration is no longer needed.
 * 
 * Provides a customized ObjectMapper bean with:
 * - ISO-8601 date format (no timestamps)
 * - Non-null property serialization
 * - Lenient deserialization (ignores unknown properties)
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build();
    }
}
