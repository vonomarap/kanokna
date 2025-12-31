package com.kanokna.catalog.application.port.in;

import com.kanokna.catalog.application.dto.ConfigurationResponse;
import com.kanokna.catalog.application.dto.ValidateConfigurationCommand;

/**
 * Inbound port: Validate a product configuration.
 */
public interface ValidateConfigurationUseCase {

    ConfigurationResponse validate(ValidateConfigurationCommand command);
}
