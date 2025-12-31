package com.kanokna.catalog.adapters.in.web;

import com.kanokna.catalog.application.port.in.GetProductTemplateQuery;
import com.kanokna.catalog.application.port.in.ListProductTemplatesQuery;
import com.kanokna.catalog.domain.model.ProductFamily;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for CatalogController.
 */
@WebMvcTest(CatalogController.class)
class CatalogControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetProductTemplateQuery getProductTemplateQuery;

    @MockBean
    private ListProductTemplatesQuery listProductTemplatesQuery;

    @Test
    @DisplayName("GET /api/catalog/products returns product list")
    void listProducts_ReturnsOk() throws Exception {
        // Given
        when(listProductTemplatesQuery.list(any(), anyBoolean())).thenReturn(List.of());

        // When/Then
        mockMvc.perform(get("/api/catalog/products"))
            .andExpect(status().isOk());
    }
}
