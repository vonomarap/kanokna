package com.kanokna.catalog.adapters.in.web;

import com.kanokna.catalog.application.dto.CreateProductTemplateCommand;
import com.kanokna.catalog.application.dto.PublishCatalogVersionCommand;
import com.kanokna.catalog.application.dto.UpdateProductTemplateCommand;
import com.kanokna.catalog.application.port.in.CreateProductTemplateUseCase;
import com.kanokna.catalog.application.port.in.PublishCatalogVersionUseCase;
import com.kanokna.catalog.application.port.in.UpdateProductTemplateUseCase;
import com.kanokna.catalog.domain.model.ProductTemplateId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * MODULE_CONTRACT id="MC-catalog-admin-rest-adapter" LAYER="adapters.in.web"
 * INTENT="Admin REST controller for catalog CRUD operations, requiring
 * CATALOG_ADMIN role"
 * LINKS="Technology.xml#TECH-spring-mvc;RequirementsAnalysis.xml#UC-CATALOG-ADMIN-MANAGE"
 *
 * REST controller for admin catalog operations. Endpoint: /api/admin/catalog
 * Requires CATALOG_ADMIN or ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/catalog")
public class AdminCatalogController {

    private final CreateProductTemplateUseCase createProductTemplateUseCase;
    private final UpdateProductTemplateUseCase updateProductTemplateUseCase;
    private final PublishCatalogVersionUseCase publishCatalogVersionUseCase;

    public AdminCatalogController(
            CreateProductTemplateUseCase createProductTemplateUseCase,
            UpdateProductTemplateUseCase updateProductTemplateUseCase,
            PublishCatalogVersionUseCase publishCatalogVersionUseCase
    ) {
        this.createProductTemplateUseCase = createProductTemplateUseCase;
        this.updateProductTemplateUseCase = updateProductTemplateUseCase;
        this.publishCatalogVersionUseCase = publishCatalogVersionUseCase;
    }

    @PostMapping("/products")
    public ResponseEntity<UUID> createProduct(@Valid @RequestBody CreateProductTemplateCommand command) {
        ProductTemplateId id = createProductTemplateUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(id.value());
    }

    @PutMapping("/products/{productTemplateId}")
    public ResponseEntity<Void> updateProduct(
            @PathVariable UUID productTemplateId,
            @Valid @RequestBody UpdateProductTemplateCommand command
    ) {
        updateProductTemplateUseCase.update(command);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/publish")
    public ResponseEntity<UUID> publishCatalogVersion(@Valid @RequestBody PublishCatalogVersionCommand command) {
        UUID catalogVersionId = publishCatalogVersionUseCase.publish(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogVersionId);
    }
}
