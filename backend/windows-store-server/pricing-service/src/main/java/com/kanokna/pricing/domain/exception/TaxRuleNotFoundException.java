package com.kanokna.pricing.domain.exception;

/**
 * Exception thrown when no tax rule is found for a region.
 * Error code: ERR-PRC-NO-TAXRULE
 */
public class TaxRuleNotFoundException extends RuntimeException {
    private final String region;

    public TaxRuleNotFoundException(String region) {
        super(String.format("No tax rule found for region: %s", region));
        this.region = region;
    }

    public String getRegion() {
        return region;
    }

    public String getErrorCode() {
        return "ERR-PRC-NO-TAXRULE";
    }
}
