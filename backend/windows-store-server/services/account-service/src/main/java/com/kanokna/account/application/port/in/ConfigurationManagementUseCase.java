package com.kanokna.account.application.port.in;

import com.kanokna.account.application.dto.DeleteConfigurationCommand;
import com.kanokna.account.application.dto.ListConfigurationsQuery;
import com.kanokna.account.application.dto.SaveConfigurationCommand;
import com.kanokna.account.application.dto.SavedConfigurationDto;

import java.util.List;

/**
 * Use case for saved configurations.
 */
public interface ConfigurationManagementUseCase {
    SavedConfigurationDto saveConfiguration(SaveConfigurationCommand command);

    List<SavedConfigurationDto> listConfigurations(ListConfigurationsQuery query);

    void deleteConfiguration(DeleteConfigurationCommand command);
}