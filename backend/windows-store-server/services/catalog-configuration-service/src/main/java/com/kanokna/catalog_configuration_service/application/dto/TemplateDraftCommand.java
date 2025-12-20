package com.kanokna.catalog_configuration_service.application.dto;

import com.kanokna.catalog_configuration_service.domain.model.DimensionPolicy;
import com.kanokna.catalog_configuration_service.domain.model.ProductType;
import com.kanokna.catalog_configuration_service.domain.model.TemplateStatus;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.i18n.LocalizedString;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record TemplateDraftCommand(
    Id templateId,
    Id tenantId,
    String name,
    ProductType productType,
    TemplateStatus status,
    DimensionPolicy dimensionPolicy,
    List<AttributeGroupDraft> attributeGroups,
    Map<String, String> mediaReferences
) {
    public TemplateDraftCommand {
        Objects.requireNonNull(templateId, "templateId");
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(productType, "productType");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(dimensionPolicy, "dimensionPolicy");
        Objects.requireNonNull(attributeGroups, "attributeGroups");
        mediaReferences = mediaReferences == null ? Map.of() : Map.copyOf(mediaReferences);
    }

    public record AttributeGroupDraft(
        String code,
        LocalizedString label,
        boolean required,
        List<AttributeOptionDraft> options
    ) {
        public AttributeGroupDraft {
            Objects.requireNonNull(code, "code");
            Objects.requireNonNull(label, "label");
            Objects.requireNonNull(options, "options");
        }
    }

    public record AttributeOptionDraft(
        String code,
        LocalizedString label,
        boolean deprecated,
        String mediaRef,
        Map<String, String> metadata
    ) {
        public AttributeOptionDraft {
            Objects.requireNonNull(code, "code");
            Objects.requireNonNull(label, "label");
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
            mediaRef = mediaRef == null ? "" : mediaRef.trim();
        }
    }
}
