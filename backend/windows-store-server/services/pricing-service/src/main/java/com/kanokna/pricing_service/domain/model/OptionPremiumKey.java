package com.kanokna.pricing_service.domain.model;

import java.util.Objects;

public record OptionPremiumKey(String attributeCode, String optionCode) {
    public OptionPremiumKey {
        if (attributeCode == null || attributeCode.isBlank()) {
            throw new IllegalArgumentException("attributeCode is required");
        }
        if (optionCode == null || optionCode.isBlank()) {
            throw new IllegalArgumentException("optionCode is required");
        }
    }

    public String asKey() {
        return attributeCode + ":" + optionCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OptionPremiumKey that)) return false;
        return attributeCode.equals(that.attributeCode) && optionCode.equals(that.optionCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributeCode, optionCode);
    }
}
