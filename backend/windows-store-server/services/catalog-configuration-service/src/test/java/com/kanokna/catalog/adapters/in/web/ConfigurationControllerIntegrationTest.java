package com.kanokna.catalog.adapters.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.catalog.application.dto.ConfigurationResponse;
import com.kanokna.catalog.application.dto.ValidateConfigurationCommand;
import com.kanokna.catalog.application.port.in.ValidateConfigurationUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ConfigurationController.
 */
@WebMvcTest(ConfigurationController.class)
class ConfigurationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ValidateConfigurationUseCase validateConfigurationUseCase;

    @Test
    @DisplayName("POST /api/catalog/configure/validate returns validation result")
    void validateConfiguration_ReturnsResult() throws Exception {
        // Given
        ValidateConfigurationCommand command = new ValidateConfigurationCommand(
            UUID.randomUUID(),
            120,
            150,
            Map.of()
        );

        ConfigurationResponse response = new ConfigurationResponse(
            true,
            List.of(),
            BigDecimal.valueOf(1000)
        );

        when(validateConfigurationUseCase.validate(any())).thenReturn(response);

        // When/Then
        mockMvc.perform(post("/api/catalog/configure/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid").value(true))
            .andExpect(jsonPath("$.priceQuote").value(1000));
    }
}
