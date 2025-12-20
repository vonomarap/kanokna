package com.kanokna.pricing_service.contracts;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ContractFilesExistTest {

    @Test
    void openApiContractExists() throws Exception {
        Path path = Path.of("docs/pricing-openapi.yaml");
        assertTrue(Files.exists(path), "OpenAPI contract missing");
        assertTrue(Files.size(path) > 0, "OpenAPI contract empty");
    }

    @Test
    void asyncApiContractExists() throws Exception {
        Path path = Path.of("docs/pricing-asyncapi.yaml");
        assertTrue(Files.exists(path), "AsyncAPI contract missing");
        assertTrue(Files.size(path) > 0, "AsyncAPI contract empty");
    }
}
