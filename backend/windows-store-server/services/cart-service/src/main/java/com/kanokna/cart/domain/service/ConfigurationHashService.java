package com.kanokna.cart.domain.service;

import com.kanokna.cart.domain.model.ConfigurationSnapshot.SelectedOptionSnapshot;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Computes deterministic configuration hashes for merge comparison.
 */
public class ConfigurationHashService {
    public String computeHash(String productTemplateId,
                              int widthCm,
                              int heightCm,
                              List<SelectedOptionSnapshot> selectedOptions) {
        Objects.requireNonNull(productTemplateId, "productTemplateId cannot be null");
        List<SelectedOptionSnapshot> sorted = selectedOptions == null
            ? List.of()
            : new ArrayList<>(selectedOptions);
        sorted.sort(Comparator
            .comparing(SelectedOptionSnapshot::optionGroupId)
            .thenComparing(SelectedOptionSnapshot::optionId));

        StringBuilder payload = new StringBuilder();
        payload.append(productTemplateId)
            .append('|')
            .append(widthCm)
            .append('x')
            .append(heightCm)
            .append('|');
        for (SelectedOptionSnapshot option : sorted) {
            payload.append(option.optionGroupId())
                .append(':')
                .append(option.optionId())
                .append('|');
        }
        return sha256(payload.toString());
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return toHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
