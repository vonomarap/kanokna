package com.kanokna.catalog.application.service;

import com.kanokna.catalog.application.dto.ProductTemplateDto;
import com.kanokna.catalog.application.port.out.ProductTemplateRepository;
import com.kanokna.catalog.domain.exception.ProductTemplateNotFoundException;
import com.kanokna.catalog.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ProductTemplateQueryService.
 */
@ExtendWith(MockitoExtension.class)
class ProductTemplateQueryServiceTest {

    @Mock
    private ProductTemplateRepository productTemplateRepository;

    private ProductTemplateQueryService queryService;

    @BeforeEach
    void setUp() {
        queryService = new ProductTemplateQueryService(productTemplateRepository);
    }

    @Test
    @DisplayName("Get by ID returns product template")
    void getById_ReturnsTemplate() {
        // Given
        ProductTemplateId id = ProductTemplateId.generate();
        ProductTemplate template = ProductTemplate.create(
            "Test Window",
            "Description",
            ProductFamily.WINDOW,
            DimensionConstraints.standard()
        );

        when(productTemplateRepository.findById(id)).thenReturn(Optional.of(template));

        // When
        ProductTemplateDto result = queryService.getById(id);

        // Then
        assertNotNull(result);
        assertEquals("Test Window", result.name());
    }

    @Test
    @DisplayName("Get by ID throws exception when not found")
    void getById_ThrowsExceptionWhenNotFound() {
        // Given
        ProductTemplateId id = ProductTemplateId.generate();
        when(productTemplateRepository.findById(id)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ProductTemplateNotFoundException.class,
            () -> queryService.getById(id));
    }

    @Test
    @DisplayName("List with filters returns matching templates")
    void list_ReturnsMatchingTemplates() {
        // Given
        ProductTemplate template = ProductTemplate.create(
            "Window",
            "Description",
            ProductFamily.WINDOW,
            DimensionConstraints.standard()
        );
        template.publish();

        when(productTemplateRepository.findByProductFamilyAndStatus(any(), any()))
            .thenReturn(List.of(template));

        // When
        List<ProductTemplateDto> results = queryService.list(ProductFamily.WINDOW, true);

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }
}
