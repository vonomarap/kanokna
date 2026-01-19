package com.kanokna.search.adapters.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kanokna.search.adapters.config.SecurityConfig;
import com.kanokna.search.application.dto.ReindexResult;
import com.kanokna.search.application.port.in.ReindexCatalogUseCase;
import com.kanokna.shared.core.DomainException;

@WebMvcTest(AdminSearchController.class)
@Import({SecurityConfig.class, AdminSearchControllerTest.TestExceptionHandler.class})
class AdminSearchControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReindexCatalogUseCase reindexCatalogUseCase;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC-FUNC-REINDEX-001: reindex_withAdminRole_startsReindex")
    void reindex_withAdminRole_startsReindex() throws Exception {
        when(reindexCatalogUseCase.reindexCatalog(any()))
            .thenReturn(new ReindexResult("product_templates_v2", 12, 1200));

        mockMvc.perform(post("/api/admin/search/reindex"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.newIndexName").value("product_templates_v2"))
            .andExpect(jsonPath("$.documentCount").value(12));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("TC-FUNC-REINDEX-006: reindex_withoutAdminRole_returns403")
    void reindex_withoutAdminRole_returns403() throws Exception {
        mockMvc.perform(post("/api/admin/search/reindex"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC-FUNC-REINDEX-003: reindex_lockInProgress_returns409Conflict")
    void reindex_lockInProgress_returns409Conflict() throws Exception {
        when(reindexCatalogUseCase.reindexCatalog(any()))
            .thenThrow(new DomainException("ERR-REINDEX-IN-PROGRESS", "lock held"));

        mockMvc.perform(post("/api/admin/search/reindex"))
            .andExpect(status().isConflict());
    }

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(DomainException.class)
        public ProblemDetail handleDomainException(DomainException ex) {
            HttpStatus status = "ERR-REINDEX-IN-PROGRESS".equals(ex.getCode())
                ? HttpStatus.CONFLICT
                : HttpStatus.INTERNAL_SERVER_ERROR;
            ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
            detail.setTitle(ex.getCode());
            return detail;
        }
    }
}
