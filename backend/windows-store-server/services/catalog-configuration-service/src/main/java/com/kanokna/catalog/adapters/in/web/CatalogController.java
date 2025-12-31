package com.kanokna.catalog.adapters.in.web;

import com.kanokna.catalog.application.dto.ProductTemplateDto;
import com.kanokna.catalog.application.port.in.GetProductTemplateQuery;
import com.kanokna.catalog.application.port.in.ListProductTemplatesQuery;
import com.kanokna.catalog.domain.model.ProductFamily;
import com.kanokna.catalog.domain.model.ProductTemplateId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for public catalog browsing operations.
 * Endpoint: /api/catalog
 */
@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final GetProductTemplateQuery getProductTemplateQuery;
    private final ListProductTemplatesQuery listProductTemplatesQuery;

    public CatalogController(
        GetProductTemplateQuery getProductTemplateQuery,
        ListProductTemplatesQuery listProductTemplatesQuery
    ) {
        this.getProductTemplateQuery = getProductTemplateQuery;
        this.listProductTemplatesQuery = listProductTemplatesQuery;
    }

    @GetMapping("/products/{productTemplateId}")
    public ResponseEntity<ProductTemplateDto> getProduct(@PathVariable UUID productTemplateId) {
        ProductTemplateDto product = getProductTemplateQuery.getById(ProductTemplateId.of(productTemplateId));
        return ResponseEntity.ok(product);
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductTemplateDto>> listProducts(
        @RequestParam(required = false) ProductFamily productFamily,
        @RequestParam(defaultValue = "true") boolean activeOnly
    ) {
        List<ProductTemplateDto> products = listProductTemplatesQuery.list(productFamily, activeOnly);
        return ResponseEntity.ok(products);
    }
}
