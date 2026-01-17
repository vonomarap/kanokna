package com.kanokna.catalog.adapters.in.grpc;

import com.kanokna.catalog.application.dto.ConfigurationResponse;
import com.kanokna.catalog.application.dto.OptionGroupDto;
import com.kanokna.catalog.application.dto.ProductTemplateDto;
import com.kanokna.catalog.application.dto.ValidateConfigurationCommand;
import com.kanokna.catalog.domain.model.Configuration;
import com.kanokna.catalog.domain.model.ProductFamily;
import com.kanokna.catalog.domain.model.ResolvedBom;
import com.kanokna.catalog.domain.model.TemplateStatus;
import com.kanokna.catalog.v1.BillOfMaterials;
import com.kanokna.catalog.v1.BomLine;
import com.kanokna.catalog.v1.DimensionConstraints;
import com.kanokna.catalog.v1.GetProductTemplateResponse;
import com.kanokna.catalog.v1.ListProductTemplatesResponse;
import com.kanokna.catalog.v1.Option;
import com.kanokna.catalog.v1.OptionGroup;
import com.kanokna.catalog.v1.ProductTemplate;
import com.kanokna.catalog.v1.ProductTemplateStatus;
import com.kanokna.catalog.v1.SelectedOption;
import com.kanokna.catalog.v1.ValidateConfigurationRequest;
import com.kanokna.catalog.v1.ValidateConfigurationResponse;
import com.kanokna.catalog.v1.ValidationError;
import com.kanokna.common.v1.Currency;
import com.kanokna.common.v1.Money;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class CatalogConfigurationGrpcMapper {

    public ValidateConfigurationCommand toCommand(ValidateConfigurationRequest request) {
        Map<String, UUID> selectedOptions = toSelectedOptions(request.getSelectedOptionsList());
        return new ValidateConfigurationCommand(
            UUID.fromString(request.getProductTemplateId()),
            request.getDimensions().getWidthCm(),
            request.getDimensions().getHeightCm(),
            selectedOptions
        );
    }

    public Configuration toConfiguration(ValidateConfigurationCommand command) {
        return new Configuration(
            command.widthCm(),
            command.heightCm(),
            command.selectedOptions()
        );
    }

    public ValidateConfigurationResponse toValidateConfigurationResponse(
        ConfigurationResponse response,
        ResolvedBom resolvedBom
    ) {
        ValidateConfigurationResponse.Builder builder = ValidateConfigurationResponse.newBuilder()
            .setValid(response.valid());

        if (response.errors() != null) {
            response.errors().forEach(error -> builder.addErrors(toValidationError(error)));
        }

        builder.setResolvedBom(toBillOfMaterials(resolvedBom));
        return builder.build();
    }

    public GetProductTemplateResponse toGetProductTemplateResponse(ProductTemplateDto template) {
        return GetProductTemplateResponse.newBuilder()
            .setTemplate(toProductTemplate(template))
            .build();
    }

    public ListProductTemplatesResponse toListProductTemplatesResponse(
        List<ProductTemplateDto> templates,
        String nextPageToken
    ) {
        ListProductTemplatesResponse.Builder builder = ListProductTemplatesResponse.newBuilder();
        if (templates == null || templates.isEmpty()) {
            return builder.setNextPageToken("").build();
        }

        templates.forEach(template -> builder.addTemplates(toProductTemplate(template)));
        builder.setNextPageToken(nextPageToken == null ? "" : nextPageToken);
        return builder.build();
    }

    public ProductFamily toProductFamily(String filter) {
        if (filter == null || filter.isBlank()) {
            return null;
        }
        return ProductFamily.valueOf(filter.trim().toUpperCase());
    }

    private Map<String, UUID> toSelectedOptions(List<SelectedOption> options) {
        if (options == null || options.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, UUID> selected = new HashMap<>();
        for (SelectedOption option : options) {
            selected.put(option.getOptionGroupId(), UUID.fromString(option.getOptionId()));
        }
        return selected;
    }

    private ValidationError toValidationError(ConfigurationResponse.ValidationErrorDto dto) {
        return ValidationError.newBuilder()
            .setCode(blankToEmpty(dto.code()))
            .setMessage(blankToEmpty(dto.message()))
            .setField(blankToEmpty(dto.field()))
            .build();
    }

    private BillOfMaterials toBillOfMaterials(ResolvedBom resolvedBom) {
        if (resolvedBom == null || resolvedBom.items() == null || resolvedBom.items().isEmpty()) {
            return BillOfMaterials.newBuilder().build();
        }
        BillOfMaterials.Builder builder = BillOfMaterials.newBuilder();
        for (ResolvedBom.BomItem item : resolvedBom.items()) {
            builder.addLines(BomLine.newBuilder()
                .setSku(item.sku())
                .setDescription(blankToEmpty(item.description()))
                .setQuantity(item.quantity())
                .build());
        }
        return builder.build();
    }

    private ProductTemplate toProductTemplate(ProductTemplateDto dto) {
        ProductTemplate.Builder builder = ProductTemplate.newBuilder()
            .setId(dto.id().toString())
            .setName(blankToEmpty(dto.name()))
            .setDescription(blankToEmpty(dto.description()))
            .setProductFamily(dto.productFamily() == null ? "" : dto.productFamily().name())
            .setStatus(mapStatus(dto.status()))
            .setOptionGroupCount(dto.optionGroups() == null ? 0 : dto.optionGroups().size());

        if (dto.dimensionConstraints() != null) {
            builder.setDimensionConstraints(DimensionConstraints.newBuilder()
                .setMinWidthCm(dto.dimensionConstraints().minWidthCm())
                .setMaxWidthCm(dto.dimensionConstraints().maxWidthCm())
                .setMinHeightCm(dto.dimensionConstraints().minHeightCm())
                .setMaxHeightCm(dto.dimensionConstraints().maxHeightCm())
                .build());
        }

        if (dto.optionGroups() != null) {
            for (OptionGroupDto group : dto.optionGroups()) {
                builder.addOptionGroups(toOptionGroup(group));
            }
        }

        return builder.build();
    }

    private OptionGroup toOptionGroup(OptionGroupDto group) {
        OptionGroup.Builder builder = OptionGroup.newBuilder()
            .setId(group.id().toString())
            .setName(blankToEmpty(group.name()))
            .setRequired(group.required());

        if (group.options() != null) {
            for (OptionGroupDto.OptionDto option : group.options()) {
                builder.addOptions(Option.newBuilder()
                    .setId(option.id().toString())
                    .setName(blankToEmpty(option.name()))
                    .setDescription(blankToEmpty(option.description()))
                    .setBasePremium(zeroMoney())
                    .build());
            }
        }
        return builder.build();
    }

    private ProductTemplateStatus mapStatus(TemplateStatus status) {
        if (status == null) {
            return ProductTemplateStatus.PRODUCT_TEMPLATE_STATUS_UNSPECIFIED;
        }
        return switch (status) {
            case ACTIVE -> ProductTemplateStatus.PRODUCT_TEMPLATE_STATUS_ACTIVE;
            case DRAFT -> ProductTemplateStatus.PRODUCT_TEMPLATE_STATUS_DRAFT;
            case ARCHIVED -> ProductTemplateStatus.PRODUCT_TEMPLATE_STATUS_ARCHIVED;
        };
    }

    private Money zeroMoney() {
        return Money.newBuilder()
            .setAmountMinor(0)
            .setCurrency(Currency.CURRENCY_UNSPECIFIED)
            .build();
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value;
    }
}
