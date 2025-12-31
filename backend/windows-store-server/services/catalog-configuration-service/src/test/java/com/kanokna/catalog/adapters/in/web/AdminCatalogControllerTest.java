package com.kanokna.catalog.adapters.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.catalog.application.dto.CreateProductTemplateCommand;
import com.kanokna.catalog.application.port.in.CreateProductTemplateUseCase;
import com.kanokna.catalog.application.port.in.PublishCatalogVersionUseCase;
import com.kanokna.catalog.application.port.in.UpdateProductTemplateUseCase;
import com.kanokna.catalog.domain.model.ProductFamily;
import com.kanokna.catalog.domain.model.ProductTemplateId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for AdminCatalogController.
 */
@WebMvcTest(AdminCatalogController.class)
class AdminCatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateProductTemplateUseCase createProductTemplateUseCase;

    @MockBean
    private UpdateProductTemplateUseCase updateProductTemplateUseCase;

    @MockBean
    private PublishCatalogVersionUseCase publishCatalogVersionUseCase;

    @Test
    @DisplayName("POST /api/admin/catalog/products creates product")
    void createProduct_ReturnsCreated() throws Exception {
        // Given
        CreateProductTemplateCommand command = new CreateProductTemplateCommand(
            "Test Window",
            "Description",
            ProductFamily.WINDOW,
            new CreateProductTemplateCommand.DimensionConstraintsDto(50, 400, 50, 400),
            List.of()
        );

        when(createProductTemplateUseCase.create(any())).thenReturn(ProductTemplateId.generate());

        // When/Then
        mockMvc.perform(post("/api/admin/catalog/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
            .andExpect(status().isCreated());
    }
}
