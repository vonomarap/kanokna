package com.kanokna.pricing_service.adapters.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record QuoteRequestDto(
    String priceBookId,
    @NotBlank String region,
    @NotBlank String currency,
    @NotBlank String itemCode,
    @NotNull Map<String, String> optionSelections,
    String customerSegment,
    @Min(0) long catalogVersion,
    boolean includeTax,
    String idempotencyKey
) { }
