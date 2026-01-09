package com.kanokna.search.adapters.in.kafka;

import com.kanokna.catalog.v1.ProductTemplatePublishedEvent;
import com.kanokna.catalog.v1.ProductTemplateUnpublishedEvent;
import com.kanokna.search.application.port.in.DeleteProductUseCase;
import com.kanokna.search.application.port.in.IndexProductUseCase;
import com.kanokna.shared.core.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SearchCatalogEventListenerTest {
    @Mock
    private IndexProductUseCase indexProductUseCase;

    @Mock
    private DeleteProductUseCase deleteProductUseCase;

    @Mock
    private Acknowledgment acknowledgment;

    private SearchCatalogEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new SearchCatalogEventListener(indexProductUseCase, deleteProductUseCase);
    }

    @Test
    @DisplayName("Invalid index events are acknowledged and skipped")
    void invalidIndexEventIsAcknowledged() {
        doThrow(new DomainException("ERR-INDEX-INVALID-EVENT", "Missing fields"))
            .when(indexProductUseCase).indexProduct(any());

        ProductTemplatePublishedEvent event = ProductTemplatePublishedEvent.newBuilder().build();

        assertDoesNotThrow(() -> listener.onProductPublished(
            event,
            acknowledgment,
            "catalog.product.published"
        ));

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Index failures propagate for retry handling")
    void indexFailurePropagates() {
        doThrow(new DomainException("ERR-INDEX-ES-UNAVAILABLE", "ES down"))
            .when(indexProductUseCase).indexProduct(any());

        ProductTemplatePublishedEvent event = ProductTemplatePublishedEvent.newBuilder().build();

        assertThrows(DomainException.class, () -> listener.onProductPublished(
            event,
            acknowledgment,
            "catalog.product.published"
        ));

        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("Invalid delete events are acknowledged and skipped")
    void invalidDeleteEventIsAcknowledged() {
        doThrow(new IllegalArgumentException("productId is required"))
            .when(deleteProductUseCase).deleteProduct(any());

        ProductTemplateUnpublishedEvent event = ProductTemplateUnpublishedEvent.newBuilder().build();

        assertDoesNotThrow(() -> listener.onProductUnpublished(
            event,
            acknowledgment,
            "catalog.product.unpublished"
        ));

        verify(acknowledgment).acknowledge();
    }
}
