package com.kanokna.catalog_configuration_service.domain.model;

import com.kanokna.catalog_configuration_service.domain.exception.CatalogDomainException;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.i18n.LocalizedString;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ProductTemplate {
    private final Id id;
    private final Id tenantId;
    private final String name;
    private final ProductType productType;
    private final TemplateStatus status;
    private final long version;
    private final DimensionPolicy dimensionPolicy;
    private final Map<String, AttributeGroup> attributeGroupsByCode;
    private final ConfigurationRuleSet ruleSet;
    private final List<String> mediaReferences;

    public ProductTemplate(
        Id id,
        Id tenantId,
        String name,
        ProductType productType,
        TemplateStatus status,
        long version,
        DimensionPolicy dimensionPolicy,
        List<AttributeGroup> attributeGroups,
        ConfigurationRuleSet ruleSet,
        List<String> mediaReferences
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.name = requireText(name, "name");
        this.productType = Objects.requireNonNull(productType, "productType");
        this.status = Objects.requireNonNull(status, "status");
        if (version < 0) {
            throw new IllegalArgumentException("version must be non-negative");
        }
        this.version = version;
        this.dimensionPolicy = Objects.requireNonNull(dimensionPolicy, "dimensionPolicy");

        Objects.requireNonNull(attributeGroups, "attributeGroups");
        Map<String, AttributeGroup> groupMap = new LinkedHashMap<>();
        for (AttributeGroup group : attributeGroups) {
            if (groupMap.containsKey(group.code())) {
                throw new CatalogDomainException("Duplicate attribute group code: " + group.code());
            }
            groupMap.put(group.code(), group);
        }
        this.attributeGroupsByCode = Collections.unmodifiableMap(groupMap);

        this.ruleSet = Objects.requireNonNullElseGet(ruleSet, () -> new ConfigurationRuleSet(List.of()));
        this.mediaReferences = Collections.unmodifiableList(mediaReferences == null ? List.of() : List.copyOf(mediaReferences));

        this.ruleSet.assertTargets(this);
    }

    public Id id() {
        return id;
    }

    public Id tenantId() {
        return tenantId;
    }

    public String name() {
        return name;
    }

    public ProductType productType() {
        return productType;
    }

    public TemplateStatus status() {
        return status;
    }

    public long version() {
        return version;
    }

    public DimensionPolicy dimensionPolicy() {
        return dimensionPolicy;
    }

    public List<AttributeGroup> attributeGroups() {
        return List.copyOf(attributeGroupsByCode.values());
    }

    public boolean hasAttribute(String attributeCode) {
        return attributeGroupsByCode.containsKey(attributeCode);
    }

    public AttributeGroup attributeGroup(String attributeCode) {
        AttributeGroup group = attributeGroupsByCode.get(attributeCode);
        if (group == null) {
            throw new CatalogDomainException("Unknown attribute group: " + attributeCode);
        }
        return group;
    }

    public boolean isActive() {
        return status == TemplateStatus.ACTIVE;
    }

    public ProductTemplate publish() {
        if (status == TemplateStatus.ACTIVE) {
            return this;
        }
        if (status == TemplateStatus.DEPRECATED) {
            throw new CatalogDomainException("Cannot publish deprecated template");
        }
        return new ProductTemplate(id, tenantId, name, productType, TemplateStatus.ACTIVE, version + 1,
            dimensionPolicy, attributeGroups(), ruleSet, mediaReferences);
    }

    public ProductTemplate deprecate() {
        return new ProductTemplate(id, tenantId, name, productType, TemplateStatus.DEPRECATED, version + 1,
            dimensionPolicy, attributeGroups(), ruleSet, mediaReferences);
    }

    public Set<String> attributeCodes() {
        return attributeGroupsByCode.keySet();
    }

    public ConfigurationRuleSet ruleSet() {
        return ruleSet;
    }

    public List<String> mediaReferences() {
        return mediaReferences;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must be provided");
        }
        return value.trim();
    }

    public record AttributeGroup(
        String code,
        LocalizedString label,
        boolean required,
        List<AttributeOption> options
    ) {
        public AttributeGroup {
            if (code == null || code.isBlank()) {
                throw new IllegalArgumentException("Attribute group code is required");
            }
            Objects.requireNonNull(label, "label");
            Objects.requireNonNull(options, "options");
            Map<String, AttributeOption> optionMap = new LinkedHashMap<>();
            for (AttributeOption option : options) {
                if (optionMap.containsKey(option.code())) {
                    throw new CatalogDomainException("Duplicate option code within group %s: %s".formatted(code, option.code()));
                }
                optionMap.put(option.code(), option);
            }
            options = List.copyOf(optionMap.values());
        }
    }

    public record AttributeOption(
        String code,
        LocalizedString label,
        boolean deprecated,
        String mediaRef,
        Map<String, String> metadata
    ) {
        public AttributeOption {
            if (code == null || code.isBlank()) {
                throw new IllegalArgumentException("Attribute option code is required");
            }
            Objects.requireNonNull(label, "label");
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
            mediaRef = mediaRef == null ? "" : mediaRef.trim();
        }
    }
}
