package com.kanokna.account.adapters.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.account.application.dto.GetProfileQuery;
import com.kanokna.account.application.dto.UpdateProfileCommand;
import com.kanokna.account.application.dto.UserProfileDto;
import com.kanokna.account.application.port.in.GetProfileUseCase;
import com.kanokna.account.application.port.in.UpdateProfileUseCase;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountProfileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private GetProfileUseCase getProfileUseCase;

    @MockitoBean
    private UpdateProfileUseCase updateProfileUseCase;

    @Test
    @DisplayName("GET /api/accounts/{userId}/profile returns profile")
    void getProfileReturnsProfile() throws Exception {
        String userId = UUID.randomUUID().toString();
        UserProfileDto profile = new UserProfileDto(
            userId,
            "user@example.com",
            "Jane",
            "Doe",
            "+79001234567",
            "ru",
            "RUB",
            null,
            List.of(),
            Instant.now(),
            Instant.now()
        );
        when(getProfileUseCase.getProfile(any())).thenReturn(profile);

        mockMvc.perform(get("/api/accounts/{userId}/profile", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(userId))
            .andExpect(jsonPath("$.email").value("user@example.com"));

        ArgumentCaptor<GetProfileQuery> captor = ArgumentCaptor.forClass(GetProfileQuery.class);
        verify(getProfileUseCase).getProfile(captor.capture());
        assertThat(captor.getValue().userId().toString()).isEqualTo(userId);
    }

    @Test
    @DisplayName("PUT /api/accounts/{userId}/profile updates profile")
    void updateProfileReturnsUpdatedProfile() throws Exception {
        String userId = UUID.randomUUID().toString();
        AccountProfileController.UpdateProfileRequest request =
            new AccountProfileController.UpdateProfileRequest("New", "Name", null, null, null);

        UserProfileDto profile = new UserProfileDto(
            userId,
            "user@example.com",
            "New",
            "Name",
            null,
            "ru",
            "RUB",
            null,
            List.of(),
            Instant.now(),
            Instant.now()
        );
        when(updateProfileUseCase.updateProfile(any())).thenReturn(profile);

        mockMvc.perform(put("/api/accounts/{userId}/profile", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("New"))
            .andExpect(jsonPath("$.lastName").value("Name"));

        ArgumentCaptor<UpdateProfileCommand> captor = ArgumentCaptor.forClass(UpdateProfileCommand.class);
        verify(updateProfileUseCase).updateProfile(captor.capture());
        assertThat(captor.getValue().userId().toString()).isEqualTo(userId);
    }
}
