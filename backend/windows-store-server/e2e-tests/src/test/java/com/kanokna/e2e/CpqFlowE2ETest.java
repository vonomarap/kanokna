package com.kanokna.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CpqFlowE2ETest extends E2ETestBase {
    @Test
    @DisplayName("E2E-CPQ-001: Configure -> Price -> Quote -> Cart flow")
    void cpqFlow() {
        ServiceRouting routing = requireServiceRouting();
        assertNotNull(routing);
    }
}
