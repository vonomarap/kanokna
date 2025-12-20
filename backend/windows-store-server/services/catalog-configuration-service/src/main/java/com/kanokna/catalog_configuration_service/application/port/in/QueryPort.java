package com.kanokna.catalog_configuration_service.application.port.in;

import com.kanokna.catalog_configuration_service.application.dto.ResolveBomCommand;
import com.kanokna.catalog_configuration_service.application.dto.ResolveBomResponse;
import com.kanokna.catalog_configuration_service.application.dto.ValidateConfigurationCommand;
import com.kanokna.catalog_configuration_service.application.dto.ValidateConfigurationResponse;

public interface QueryPort {

    ValidateConfigurationResponse validateConfiguration(ValidateConfigurationCommand command);

    ResolveBomResponse resolveBom(ResolveBomCommand command);

    // Future: listTemplates, getTemplate, queryOptions to be implemented per DevelopmentPlan flows.
}
