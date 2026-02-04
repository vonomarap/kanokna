package com.kanokna.search.adapters.in.web;

import com.kanokna.search.application.dto.ReindexCommand;
import com.kanokna.search.application.dto.ReindexResult;
import com.kanokna.search.application.port.in.ReindexCatalogUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * MODULE_CONTRACT id="MC-search-admin-rest-adapter" LAYER="adapters.in.web"
 * INTENT="Admin REST controller for search operations, requiring ADMIN role"
 * LINKS="Technology.xml#TECH-spring-mvc;RequirementsAnalysis.xml#NFR-SEC-RBAC"
 *
 * REST controller for admin search operations. Endpoint: /api/admin/search
 * Requires ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/search")
public class AdminSearchController {

    private final ReindexCatalogUseCase reindexCatalogUseCase;

    public AdminSearchController(ReindexCatalogUseCase reindexCatalogUseCase) {
        this.reindexCatalogUseCase = reindexCatalogUseCase;
    }

    @PostMapping("/reindex")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReindexResult> reindex(@RequestParam(required = false) String sourceIndex) {
        ReindexResult result = reindexCatalogUseCase.reindexCatalog(new ReindexCommand(sourceIndex));
        return ResponseEntity.ok(result);
    }
}
