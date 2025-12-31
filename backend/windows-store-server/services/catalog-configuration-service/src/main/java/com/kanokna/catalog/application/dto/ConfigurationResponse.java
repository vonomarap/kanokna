package com.kanokna.catalog.application.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for configuration validation with optional pricing.
 */
public record ConfigurationResponse(
    boolean valid,
    List<ValidationErrorDto> errors,
    BigDecimal priceQuote
) {

    public record ValidationErrorDto(
        String code,
        String message,
        String field
    ) {
    }
}
