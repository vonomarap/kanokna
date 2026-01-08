package com.kanokna.account.application.port.in;

import com.kanokna.account.application.dto.AddAddressCommand;
import com.kanokna.account.application.dto.DeleteAddressCommand;
import com.kanokna.account.application.dto.ListAddressesQuery;
import com.kanokna.account.application.dto.SavedAddressDto;
import com.kanokna.account.application.dto.UpdateAddressCommand;

import java.util.List;

/**
 * Use case for managing addresses.
 */
public interface AddressManagementUseCase {
    SavedAddressDto addAddress(AddAddressCommand command);

    SavedAddressDto updateAddress(UpdateAddressCommand command);

    void deleteAddress(DeleteAddressCommand command);

    List<SavedAddressDto> listAddresses(ListAddressesQuery query);
}