package com.kanokna.catalog.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.catalog.application.dto.CreateProductTemplateCommand;
import com.kanokna.catalog.application.dto.PublishCatalogVersionCommand;
import com.kanokna.catalog.application.dto.UpdateProductTemplateCommand;
import com.kanokna.catalog.application.port.out.CatalogVersionRepository;
import com.kanokna.catalog.application.port.out.EventPublisher;
import com.kanokna.catalog.application.port.out.ProductTemplateRepository;
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
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductTemplateCommandService.
 * Covers admin FUNCTION_CONTRACTs:
 * - FC-...-createProductTemplate (TC-ADMIN-CREATE-*)
 * - FC-...-updateProductTemplate (TC-ADMIN-UPDATE-*)
 * - FC-...-publishCatalogVersion (TC-ADMIN-PUBLISH-*)
 */
@ExtendWith(MockitoExtension.class)
class ProductTemplateCommandServiceTest {

    @Mock
    private ProductTemplateRepository productTemplateRepository;

    @Mock
    private CatalogVersionRepository catalogVersionRepository;

    @Mock
    private EventPublisher eventPublisher;

    private ProductTemplateCommandService commandService;

    @BeforeEach
    void setUp() {
        commandService = new ProductTemplateCommandService(
            productTemplateRepository,
            catalogVersionRepository,
            eventPublisher,
            new ObjectMapper()
        );
    }

    @Test
    @DisplayName("TC-ADMIN-CREATE-001: Valid command creates template in DRAFT status")
    void validCommand_CreatesTemplateInDraft() {
        // Given
        CreateProductTemplateCommand command = new CreateProductTemplateCommand(
            "Test Window",
            "Description",
            ProductFamily.WINDOW,
            new CreateProductTemplateCommand.DimensionConstraintsDto(50, 400, 50, 400),
            List.of()
        );

        ProductTemplate savedTemplate = ProductTemplate.create(
            "Test Window",
            "Description",
            ProductFamily.WINDOW,
            DimensionConstraints.standard()
        );

        when(productTemplateRepository.existsByNameAndProductFamily(any(), any())).thenReturn(false);
        when(productTemplateRepository.save(any())).thenReturn(savedTemplate);

        // When
        ProductTemplateId result = commandService.create(command);

        // Then
        assertNotNull(result);
        verify(productTemplateRepository).save(any(ProductTemplate.class));
    }

    @Test
    @DisplayName("TC-ADMIN-CREATE-002: Duplicate name returns ERR-CATALOG-DUPLICATE-NAME")
    void duplicateName_ThrowsException() {
        // Given
        CreateProductTemplateCommand command = new CreateProductTemplateCommand(
            "Duplicate",
            "Description",
            ProductFamily.WINDOW,
            new CreateProductTemplateCommand.DimensionConstraintsDto(50, 400, 50, 400),
            List.of()
        );

        when(productTemplateRepository.existsByNameAndProductFamily(any(), any())).thenReturn(true);

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> commandService.create(command));
    }

    @Test
    @DisplayName("TC-ADMIN-UPDATE-001: Update DRAFT template succeeds in-place")
    void updateDraft_SucceedsInPlace() {
        // Given
        ProductTemplateId id = ProductTemplateId.generate();
        ProductTemplate draftTemplate = ProductTemplate.create(
            "Original",
            "Description",
            ProductFamily.WINDOW,
            DimensionConstraints.standard()
        );

        UpdateProductTemplateCommand command = new UpdateProductTemplateCommand(
            id.value(),
            "Updated Name",
            "Updated Description",
            new UpdateProductTemplateCommand.DimensionConstraintsDto(50, 400, 50, 400),
            0
        );

        when(productTemplateRepository.findById(id)).thenReturn(Optional.of(draftTemplate));
        when(productTemplateRepository.save(any())).thenReturn(draftTemplate);

        // When
        assertDoesNotThrow(() -> commandService.update(command));

        // Then
        verify(productTemplateRepository).save(any());
    }

    @Test
    @DisplayName("TC-ADMIN-PUBLISH-001: Publish DRAFT templates creates new CatalogVersion")
    void publishDrafts_CreatesNewCatalogVersion() {
        // Given
        ProductTemplate draftTemplate = ProductTemplate.create(
            "Draft",
            "Description",
            ProductFamily.WINDOW,
            DimensionConstraints.standard()
        );

        PublishCatalogVersionCommand command = new PublishCatalogVersionCommand(
            "admin-user",
            null
        );

        when(productTemplateRepository.findByStatus(TemplateStatus.DRAFT))
            .thenReturn(List.of(draftTemplate));
        when(catalogVersionRepository.getNextVersionNumber()).thenReturn(1);
        when(catalogVersionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(productTemplateRepository.findByProductFamilyAndStatus(any(), any()))
            .thenReturn(List.of());
        when(productTemplateRepository.save(any())).thenReturn(draftTemplate);

        // When
        assertDoesNotThrow(() -> commandService.publish(command));

        // Then
        verify(catalogVersionRepository).save(any(CatalogVersion.class));
        verify(eventPublisher, atLeastOnce()).publish(anyString(), any());
    }

    @Test
    @DisplayName("TC-ADMIN-PUBLISH-005: No DRAFT templates returns ERR-CATALOG-NOTHING-TO-PUBLISH")
    void noDraftTemplates_ThrowsException() {
        // Given
        PublishCatalogVersionCommand command = new PublishCatalogVersionCommand(
            "admin-user",
            null
        );

        when(productTemplateRepository.findByStatus(TemplateStatus.DRAFT)).thenReturn(List.of());

        // When/Then
        assertThrows(IllegalStateException.class, () -> commandService.publish(command));
    }
}
