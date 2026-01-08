package com.kanokna.account.domain.model;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserProfileTest {

    @Test
    @Disabled("Order-in-progress constraint not implemented in MVP")
    @DisplayName("TC-ACCT-010: Cannot delete the only address if order in progress (future)")
    void cannotDeleteOnlyAddressIfOrderInProgress() {
        // Future behavior: delete should be blocked when active orders exist.
    }
}
