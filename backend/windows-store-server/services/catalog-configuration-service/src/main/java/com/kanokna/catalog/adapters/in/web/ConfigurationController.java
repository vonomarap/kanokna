package com.kanokna.catalog.adapters.in.web;

import com.kanokna.catalog.application.dto.ConfigurationResponse;
import com.kanokna.catalog.application.dto.ValidateConfigurationCommand;
import com.kanokna.catalog.application.port.in.ValidateConfigurationUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for configuration validation operations.
 * Endpoint: /api/catalog/configure
 */
@RestController
@RequestMapping("/api/catalog/configure")
public class ConfigurationController {

    private final ValidateConfigurationUseCase validateConfigurationUseCase;

    public ConfigurationController(ValidateConfigurationUseCase validateConfigurationUseCase) {
        this.validateConfigurationUseCase = validateConfigurationUseCase;
    }

    @PostMapping("/validate")
    public ResponseEntity<ConfigurationResponse> validateConfiguration(
        @Valid @RequestBody ValidateConfigurationCommand command
    ) {
        ConfigurationResponse response = validateConfigurationUseCase.validate(command);
        return ResponseEntity.ok(response);
    }
}
