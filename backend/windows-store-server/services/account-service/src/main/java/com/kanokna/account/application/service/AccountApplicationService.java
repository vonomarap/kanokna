package com.kanokna.account.application.service;

import com.kanokna.account.application.dto.AddAddressCommand;
import com.kanokna.account.application.dto.DeleteAddressCommand;
import com.kanokna.account.application.dto.DeleteConfigurationCommand;
import com.kanokna.account.application.dto.GetProfileQuery;
import com.kanokna.account.application.dto.ListAddressesQuery;
import com.kanokna.account.application.dto.ListConfigurationsQuery;
import com.kanokna.account.application.dto.SaveConfigurationCommand;
import com.kanokna.account.application.dto.SavedAddressDto;
import com.kanokna.account.application.dto.SavedConfigurationDto;
import com.kanokna.account.application.dto.UpdateAddressCommand;
import com.kanokna.account.application.dto.UpdateProfileCommand;
import com.kanokna.account.application.dto.UserProfileDto;
import com.kanokna.account.application.port.in.AddressManagementUseCase;
import com.kanokna.account.application.port.in.ConfigurationManagementUseCase;
import com.kanokna.account.application.port.in.GetProfileUseCase;
import com.kanokna.account.application.port.in.UpdateProfileUseCase;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * MODULE_CONTRACT id="MC-account-application-facade"
 * LAYER="application.service" INTENT="Facade service implementing account use
 * case interfaces, delegating to specialized services"
 * LINKS="Technology.xml#DEC-ACCOUNT-DECOMPOSITION;RequirementsAnalysis.xml#UC-ACCOUNT-MANAGE-PROFILE"
 *
 * Facade service implementing account use case interfaces. Delegates to
 * specialized services for each responsibility group.
 *
 * @see ProfileService
 * @see AddressService
 * @see SavedConfigurationService
 */
@Service
@Transactional
public class AccountApplicationService implements
        GetProfileUseCase,
        UpdateProfileUseCase,
        AddressManagementUseCase,
        ConfigurationManagementUseCase {

    private final ProfileService profileService;
    private final AddressService addressService;
    private final SavedConfigurationService savedConfigurationService;

    public AccountApplicationService(
            ProfileService profileService,
            AddressService addressService,
            SavedConfigurationService savedConfigurationService
    ) {
        this.profileService = profileService;
        this.addressService = addressService;
        this.savedConfigurationService = savedConfigurationService;
    }

    // ========== Profile Operations ==========
    @Override
    public UserProfileDto getProfile(GetProfileQuery query) {
        return profileService.getProfile(query);
    }

    @Override
    public UserProfileDto updateProfile(UpdateProfileCommand command) {
        return profileService.updateProfile(command);
    }

    // ========== Address Operations ==========
    @Override
    public SavedAddressDto addAddress(AddAddressCommand command) {
        return addressService.addAddress(command);
    }

    @Override
    public SavedAddressDto updateAddress(UpdateAddressCommand command) {
        return addressService.updateAddress(command);
    }

    @Override
    public void deleteAddress(DeleteAddressCommand command) {
        addressService.deleteAddress(command);
    }

    @Override
    public List<SavedAddressDto> listAddresses(ListAddressesQuery query) {
        return addressService.listAddresses(query);
    }

    // ========== Configuration Operations ==========
    @Override
    public SavedConfigurationDto saveConfiguration(SaveConfigurationCommand command) {
        return savedConfigurationService.saveConfiguration(command);
    }

    @Override
    public List<SavedConfigurationDto> listConfigurations(ListConfigurationsQuery query) {
        return savedConfigurationService.listConfigurations(query);
    }

    @Override
    public void deleteConfiguration(DeleteConfigurationCommand command) {
        savedConfigurationService.deleteConfiguration(command);
    }
}
