package com.kanokna.catalog.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO for option groups.
 */
public record OptionGroupDto(
    UUID id,
    String name,
    int displayOrder,
    boolean required,
    boolean multiSelect,
    List<OptionDto> options
) {

    public record OptionDto(
        UUID id,
        String name,
        String description,
        String skuCode,
        int displayOrder,
        boolean defaultSelected
    ) {
    }
}
