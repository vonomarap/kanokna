package com.kanokna.account.adapters.in.web;

import com.kanokna.account.application.dto.*;
import com.kanokna.account.application.port.in.ConfigurationManagementUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for saved configuration management.
 * Endpoint: /api/accounts/{userId}/configurations
 */
@RestController
@RequestMapping("/api/accounts/{userId}/configurations")
public class AccountConfigurationController {
    private final ConfigurationManagementUseCase configurationManagementUseCase;

    public AccountConfigurationController(ConfigurationManagementUseCase configurationManagementUseCase) {
        this.configurationManagementUseCase = configurationManagementUseCase;
    }

    @PostMapping
    public ResponseEntity<SavedConfigurationDto> saveConfiguration(
        @PathVariable UUID userId,
        @Valid @RequestBody SaveConfigurationRequest request
    ) {
        SaveConfigurationCommand command = new SaveConfigurationCommand(
            userId,
            request.name(),
            request.productTemplateId(),
            request.configurationSnapshot(),
            request.quoteSnapshot()
        );
        SavedConfigurationDto saved = configurationManagementUseCase.saveConfiguration(command);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<SavedConfigurationDto>> listConfigurations(@PathVariable UUID userId) {
        List<SavedConfigurationDto> configs = configurationManagementUseCase.listConfigurations(
            new ListConfigurationsQuery(userId)
        );
        return ResponseEntity.ok(configs);
    }

    @DeleteMapping("/{configurationId}")
    public ResponseEntity<Void> deleteConfiguration(
        @PathVariable UUID userId,
        @PathVariable UUID configurationId
    ) {
        configurationManagementUseCase.deleteConfiguration(new DeleteConfigurationCommand(userId, configurationId));
        return ResponseEntity.noContent().build();
    }

    public record SaveConfigurationRequest(
        @NotBlank String name,
        @NotNull UUID productTemplateId,
        @NotBlank String configurationSnapshot,
        String quoteSnapshot
    ) {
    }
}
