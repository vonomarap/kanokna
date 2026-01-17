package com.kanokna.pricing.adapters.out.redis;

import com.kanokna.pricing.application.dto.CalculateQuoteCommand;
import com.kanokna.pricing.domain.model.PriceBook;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cache key generator for quote caching.
 */
public final class QuoteCacheKey {
    private static final String PREFIX = "quote:product:";
    private final String value;

    private QuoteCacheKey(String value) {
        this.value = value;
    }

    public static QuoteCacheKey from(PriceBook priceBook, CalculateQuoteCommand command) {
        String raw = String.join("|",
            command.getProductTemplateId(),
            priceBook.getId().toString(),
            String.valueOf(priceBook.getVersion()),
            command.getWidthCm().toPlainString(),
            command.getHeightCm().toPlainString(),
            normalizeOptions(command.getResolvedBom()),
            command.getCurrency(),
            command.getPromoCode() == null ? "" : command.getPromoCode().trim().toUpperCase(),
            command.getRegion()
        );

        String hash = sha256(raw);
        return new QuoteCacheKey(PREFIX + command.getProductTemplateId() + ":" + hash);
    }

    public String value() {
        return value;
    }

    private static String normalizeOptions(List<String> options) {
        if (options == null || options.isEmpty()) {
            return "";
        }
        return options.stream()
            .sorted()
            .collect(Collectors.joining(","));
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash quote cache key", ex);
        }
    }
}
