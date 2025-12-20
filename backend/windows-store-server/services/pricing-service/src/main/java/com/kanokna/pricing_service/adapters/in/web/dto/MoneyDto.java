package com.kanokna.pricing_service.adapters.in.web.dto;

import java.math.BigDecimal;

public record MoneyDto(
    BigDecimal amount,
    String currency
) { }
