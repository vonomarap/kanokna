package com.kanokna.catalog.application.service;

import com.kanokna.catalog.application.dto.OptionGroupDto;
import com.kanokna.catalog.application.dto.ProductTemplateDto;
import com.kanokna.catalog.application.port.in.GetProductTemplateQuery;
import com.kanokna.catalog.application.port.in.ListProductTemplatesQuery;
import com.kanokna.catalog.application.port.out.ProductTemplateRepository;
import com.kanokna.catalog.domain.exception.ProductTemplateNotFoundException;
import com.kanokna.catalog.domain.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Application service implementing product template query operations.
 */
@Service
@Transactional(readOnly = true)
public class ProductTemplateQueryService implements GetProductTemplateQuery, ListProductTemplatesQuery {

    private final ProductTemplateRepository productTemplateRepository;

    public ProductTemplateQueryService(ProductTemplateRepository productTemplateRepository) {
        this.productTemplateRepository = productTemplateRepository;
    }

    @Override
    public ProductTemplateDto getById(ProductTemplateId productTemplateId) {
        ProductTemplate productTemplate = productTemplateRepository.findById(productTemplateId)
            .orElseThrow(() -> new ProductTemplateNotFoundException(productTemplateId));

        return mapToDto(productTemplate);
    }

    @Override
    public List<ProductTemplateDto> list(ProductFamily productFamily, boolean activeOnly) {
        List<ProductTemplate> templates;

        if (productFamily != null && activeOnly) {
            templates = productTemplateRepository.findByProductFamilyAndStatus(productFamily, TemplateStatus.ACTIVE);
        } else if (productFamily != null) {
            templates = productTemplateRepository.findAll().stream()
                .filter(t -> t.getProductFamily() == productFamily)
                .toList();
        } else if (activeOnly) {
            templates = productTemplateRepository.findByStatus(TemplateStatus.ACTIVE);
        } else {
            templates = productTemplateRepository.findAll();
        }

        return templates.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    private ProductTemplateDto mapToDto(ProductTemplate template) {
        var dimensionsDto = new ProductTemplateDto.DimensionConstraintsDto(
            template.getDimensionConstraints().minWidthCm(),
            template.getDimensionConstraints().maxWidthCm(),
            template.getDimensionConstraints().minHeightCm(),
            template.getDimensionConstraints().maxHeightCm()
        );

        var optionGroupDtos = template.getOptionGroups().stream()
            .map(this::mapOptionGroupToDto)
            .collect(Collectors.toList());

        return new ProductTemplateDto(
            template.getId().value(),
            template.getName(),
            template.getDescription(),
            template.getProductFamily(),
            dimensionsDto,
            template.getStatus(),
            template.getVersion(),
            optionGroupDtos
        );
    }

    private OptionGroupDto mapOptionGroupToDto(OptionGroup optionGroup) {
        var optionDtos = optionGroup.getOptions().stream()
            .map(opt -> new OptionGroupDto.OptionDto(
                opt.getId(),
                opt.getName(),
                opt.getDescription(),
                opt.getSkuCode(),
                opt.getDisplayOrder(),
                opt.isDefaultSelected()
            ))
            .collect(Collectors.toList());

        return new OptionGroupDto(
            optionGroup.getId(),
            optionGroup.getName(),
            optionGroup.getDisplayOrder(),
            optionGroup.isRequired(),
            optionGroup.isMultiSelect(),
            optionDtos
        );
    }
}
