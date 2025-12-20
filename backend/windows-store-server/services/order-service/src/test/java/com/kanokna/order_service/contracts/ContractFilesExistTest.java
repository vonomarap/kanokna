package com.kanokna.order_service.contracts;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ContractFilesExistTest {

    @Test
    void openApiExists() throws Exception {
        Path path = Path.of("docs/order-openapi.yaml");
        assertTrue(Files.exists(path), "order-openapi.yaml missing");
        assertTrue(Files.size(path) > 0, "order-openapi.yaml empty");
    }

    @Test
    void asyncApiExists() throws Exception {
        Path path = Path.of("docs/order-asyncapi.yaml");
        assertTrue(Files.exists(path), "order-asyncapi.yaml missing");
        assertTrue(Files.size(path) > 0, "order-asyncapi.yaml empty");
    }
}
