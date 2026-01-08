package com.kanokna.account.adapters.in.web;

import com.kanokna.account.application.dto.*;
import com.kanokna.account.application.port.in.AddressManagementUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for address management.
 * Endpoint: /api/accounts/{userId}/addresses
 */
@RestController
@RequestMapping("/api/accounts/{userId}/addresses")
public class AccountAddressController {
    private final AddressManagementUseCase addressManagementUseCase;

    public AccountAddressController(AddressManagementUseCase addressManagementUseCase) {
        this.addressManagementUseCase = addressManagementUseCase;
    }

    @PostMapping
    public ResponseEntity<SavedAddressDto> addAddress(
        @PathVariable UUID userId,
        @Valid @RequestBody AddAddressRequest request
    ) {
        AddAddressCommand command = new AddAddressCommand(
            userId,
            request.address(),
            request.label(),
            request.setAsDefault()
        );
        SavedAddressDto address = addressManagementUseCase.addAddress(command);
        return ResponseEntity.ok(address);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<SavedAddressDto> updateAddress(
        @PathVariable UUID userId,
        @PathVariable UUID addressId,
        @Valid @RequestBody UpdateAddressRequest request
    ) {
        UpdateAddressCommand command = new UpdateAddressCommand(
            userId,
            addressId,
            request.address(),
            request.label(),
            request.setAsDefault()
        );
        SavedAddressDto address = addressManagementUseCase.updateAddress(command);
        return ResponseEntity.ok(address);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
        @PathVariable UUID userId,
        @PathVariable UUID addressId
    ) {
        addressManagementUseCase.deleteAddress(new DeleteAddressCommand(userId, addressId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<SavedAddressDto>> listAddresses(@PathVariable UUID userId) {
        List<SavedAddressDto> addresses = addressManagementUseCase.listAddresses(
            new ListAddressesQuery(userId)
        );
        return ResponseEntity.ok(addresses);
    }

    public record AddAddressRequest(
        @Valid AddressDto address,
        @NotBlank String label,
        boolean setAsDefault
    ) {
    }

    public record UpdateAddressRequest(
        @Valid AddressDto address,
        String label,
        Boolean setAsDefault
    ) {
    }
}
