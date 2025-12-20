package com.kanokna.catalog_configuration_service.application.dto;

import com.kanokna.catalog_configuration_service.domain.model.DecisionTrace;
import com.kanokna.catalog_configuration_service.domain.model.ValidationMessage;

import java.util.List;

public record ValidateConfigurationResponse(
    boolean valid,
    List<ValidationMessage> errors,
    List<ValidationMessage> warnings,
    List<DecisionTrace> traces
) { }
