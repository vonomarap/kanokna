package com.kanokna.account.adapters.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.account.application.dto.ListConfigurationsQuery;
import com.kanokna.account.application.dto.SavedConfigurationDto;
import com.kanokna.account.application.port.in.ConfigurationManagementUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountConfigurationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountConfigurationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConfigurationManagementUseCase configurationManagementUseCase;

    @Test
    @DisplayName("POST /api/accounts/{userId}/configurations saves configuration")
    void saveConfiguration() throws Exception {
        String userId = UUID.randomUUID().toString();
        UUID productTemplateId = UUID.randomUUID();
        SavedConfigurationDto saved = sampleConfig();
        when(configurationManagementUseCase.saveConfiguration(any())).thenReturn(saved);

        AccountConfigurationController.SaveConfigurationRequest request =
            new AccountConfigurationController.SaveConfigurationRequest(
                "Config A",
                productTemplateId,
                "{\"productTemplateId\":\"" + productTemplateId + "\",\"dimensions\":{\"width\":100,\"height\":120,\"unit\":\"mm\"}}",
                null
            );

        mockMvc.perform(post("/api/accounts/{userId}/configurations", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Config A"));
    }

    @Test
    @DisplayName("GET /api/accounts/{userId}/configurations lists configurations")
    void listConfigurations() throws Exception {
        String userId = UUID.randomUUID().toString();
        when(configurationManagementUseCase.listConfigurations(any()))
            .thenReturn(List.of(sampleConfig()));

        mockMvc.perform(get("/api/accounts/{userId}/configurations", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Config A"));

        ArgumentCaptor<ListConfigurationsQuery> captor = ArgumentCaptor.forClass(ListConfigurationsQuery.class);
        verify(configurationManagementUseCase).listConfigurations(captor.capture());
        assertThat(captor.getValue().userId().toString()).isEqualTo(userId);
    }

    @Test
    @DisplayName("DELETE /api/accounts/{userId}/configurations/{configId} removes configuration")
    void deleteConfiguration() throws Exception {
        String userId = UUID.randomUUID().toString();
        String configId = UUID.randomUUID().toString();

        mockMvc.perform(delete("/api/accounts/{userId}/configurations/{configId}", userId, configId))
            .andExpect(status().isNoContent());

        verify(configurationManagementUseCase).deleteConfiguration(any());
    }

    private SavedConfigurationDto sampleConfig() {
        return new SavedConfigurationDto(
            UUID.randomUUID().toString(),
            "Config A",
            UUID.randomUUID().toString(),
            "{\"productTemplateId\":\"" + UUID.randomUUID() + "\",\"dimensions\":{\"width\":100,\"height\":120,\"unit\":\"mm\"}}",
            null,
            Instant.now(),
            Instant.now()
        );
    }
}
