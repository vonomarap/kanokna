package com.kanokna.catalog.domain.exception;

import com.kanokna.catalog.domain.model.ProductTemplateId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProductTemplateNotFoundException.
 */
class ProductTemplateNotFoundExceptionTest {

    @Test
    @DisplayName("Exception contains product template ID")
    void exception_ContainsTemplateId() {
        // Given
        ProductTemplateId id = ProductTemplateId.generate();

        // When
        ProductTemplateNotFoundException exception = new ProductTemplateNotFoundException(id);

        // Then
        assertEquals(id, exception.getProductTemplateId());
        assertTrue(exception.getMessage().contains(id.toString()));
    }
}
