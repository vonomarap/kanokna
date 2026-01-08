package com.kanokna.account.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.account.domain.exception.AccountDomainErrors;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Validates configuration snapshot JSON against the expected schema.
 */
@Component
public class ConfigurationSnapshotValidator {
    private final ObjectMapper objectMapper;

    public ConfigurationSnapshotValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void validate(UUID expectedProductTemplateId, String snapshotJson) {
        if (snapshotJson == null || snapshotJson.isBlank()) {
            throw AccountDomainErrors.invalidConfigSnapshot("Configuration snapshot is blank");
        }

        JsonNode root = parseRoot(snapshotJson);
        List<String> errors = new ArrayList<>();

        if (!root.isObject()) {
            errors.add("root must be an object");
        }

        String productTemplateId = readText(root, "productTemplateId", errors);
        UUID parsedProductTemplateId = parseUuid(productTemplateId, "productTemplateId", errors);
        if (expectedProductTemplateId != null && parsedProductTemplateId != null
            && !expectedProductTemplateId.equals(parsedProductTemplateId)) {
            errors.add("productTemplateId mismatch");
        }

        JsonNode dimensions = root.path("dimensions");
        if (!dimensions.isObject()) {
            errors.add("dimensions must be an object");
        } else {
            if (!dimensions.path("width").isNumber()) {
                errors.add("dimensions.width must be a number");
            }
            if (!dimensions.path("height").isNumber()) {
                errors.add("dimensions.height must be a number");
            }
            String unit = dimensions.path("unit").asText(null);
            if (unit == null || !"mm".equals(unit)) {
                errors.add("dimensions.unit must be 'mm'");
            }
        }

        validateSelectedOptions(root.path("selectedOptions"), errors);
        validateAccessories(root.path("accessories"), errors);

        if (root.has("notes") && !root.path("notes").isTextual()) {
            errors.add("notes must be a string");
        }

        if (!errors.isEmpty()) {
            throw AccountDomainErrors.invalidConfigSnapshot("Configuration snapshot invalid: " + String.join(", ", errors));
        }
    }

    private JsonNode parseRoot(String snapshotJson) {
        try {
            return objectMapper.readTree(snapshotJson);
        } catch (Exception ex) {
            throw AccountDomainErrors.invalidConfigSnapshot("Configuration snapshot is not valid JSON");
        }
    }

    private String readText(JsonNode root, String field, List<String> errors) {
        JsonNode node = root.get(field);
        if (node == null || !node.isTextual()) {
            errors.add(field + " must be a string");
            return null;
        }
        return node.asText();
    }

    private UUID parseUuid(String value, String field, List<String> errors) {
        if (value == null) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            errors.add(field + " must be a valid UUID");
            return null;
        }
    }

    private void validateSelectedOptions(JsonNode selectedOptions, List<String> errors) {
        if (selectedOptions.isMissingNode() || selectedOptions.isNull()) {
            return;
        }
        if (!selectedOptions.isArray()) {
            errors.add("selectedOptions must be an array");
            return;
        }
        for (JsonNode option : selectedOptions) {
            if (!option.isObject()) {
                errors.add("selectedOptions entry must be an object");
                continue;
            }
            parseUuid(option.path("optionGroupId").asText(null), "selectedOptions.optionGroupId", errors);
            parseUuid(option.path("optionId").asText(null), "selectedOptions.optionId", errors);
        }
    }

    private void validateAccessories(JsonNode accessories, List<String> errors) {
        if (accessories.isMissingNode() || accessories.isNull()) {
            return;
        }
        if (!accessories.isArray()) {
            errors.add("accessories must be an array");
            return;
        }
        for (JsonNode accessory : accessories) {
            if (!accessory.isObject()) {
                errors.add("accessories entry must be an object");
                continue;
            }
            parseUuid(accessory.path("accessoryId").asText(null), "accessories.accessoryId", errors);
            JsonNode quantity = accessory.path("quantity");
            if (!quantity.isInt() || quantity.asInt() < 1) {
                errors.add("accessories.quantity must be an integer >= 1");
            }
        }
    }
}
