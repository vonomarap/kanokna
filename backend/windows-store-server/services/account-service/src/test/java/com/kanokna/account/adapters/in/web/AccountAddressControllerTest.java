package com.kanokna.account.adapters.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.account.application.dto.*;
import com.kanokna.account.application.port.in.AddressManagementUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountAddressController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountAddressControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private AddressManagementUseCase addressManagementUseCase;

    @Test
    @DisplayName("POST /api/accounts/{userId}/addresses adds address")
    void addAddress() throws Exception {
        String userId = UUID.randomUUID().toString();
        SavedAddressDto saved = sampleAddress();
        when(addressManagementUseCase.addAddress(any())).thenReturn(saved);

        AccountAddressController.AddAddressRequest request = new AccountAddressController.AddAddressRequest(
            new AddressDto("RU", "Moscow", "123456", "Main Street", null),
            "Home",
            true
        );

        mockMvc.perform(post("/api/accounts/{userId}/addresses", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.label").value("Home"))
            .andExpect(jsonPath("$.isDefault").value(true));
    }

    @Test
    @DisplayName("PUT /api/accounts/{userId}/addresses/{addressId} updates address")
    void updateAddress() throws Exception {
        String userId = UUID.randomUUID().toString();
        String addressId = UUID.randomUUID().toString();
        SavedAddressDto saved = sampleAddress();
        when(addressManagementUseCase.updateAddress(any())).thenReturn(saved);

        AccountAddressController.UpdateAddressRequest request = new AccountAddressController.UpdateAddressRequest(
            new AddressDto("RU", "Moscow", "123456", "Main Street", null),
            "Office",
            false
        );

        mockMvc.perform(put("/api/accounts/{userId}/addresses/{addressId}", userId, addressId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.label").value("Home"));
    }

    @Test
    @DisplayName("GET /api/accounts/{userId}/addresses lists addresses")
    void listAddresses() throws Exception {
        String userId = UUID.randomUUID().toString();
        when(addressManagementUseCase.listAddresses(any())).thenReturn(List.of(sampleAddress()));

        mockMvc.perform(get("/api/accounts/{userId}/addresses", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].label").value("Home"));
    }

    @Test
    @DisplayName("DELETE /api/accounts/{userId}/addresses/{addressId} removes address")
    void deleteAddress() throws Exception {
        String userId = UUID.randomUUID().toString();
        String addressId = UUID.randomUUID().toString();

        mockMvc.perform(delete("/api/accounts/{userId}/addresses/{addressId}", userId, addressId))
            .andExpect(status().isNoContent());

        verify(addressManagementUseCase).deleteAddress(any());
    }

    private SavedAddressDto sampleAddress() {
        return new SavedAddressDto(
            UUID.randomUUID().toString(),
            new AddressDto("RU", "Moscow", "123456", "Main Street", null),
            "Home",
            true,
            Instant.now(),
            Instant.now()
        );
    }
}
