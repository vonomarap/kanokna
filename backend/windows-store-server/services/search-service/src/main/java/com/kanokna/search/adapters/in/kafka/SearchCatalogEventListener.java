package com.kanokna.search.adapters.in.kafka;

import java.time.Instant;

import com.kanokna.common.v1.EventMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.google.protobuf.Timestamp;
import com.kanokna.catalog.v1.ProductTemplatePublishedEvent;
import com.kanokna.catalog.v1.ProductTemplateUnpublishedEvent;
import com.kanokna.catalog.v1.ProductTemplateUpdatedEvent;
import com.kanokna.common.v1.Currency;
import com.kanokna.search.application.dto.CatalogProductDeleteEvent;
import com.kanokna.search.application.dto.CatalogProductEvent;
import com.kanokna.search.application.port.in.DeleteProductUseCase;
import com.kanokna.search.application.port.in.IndexProductUseCase;
import com.kanokna.search.domain.model.ProductStatus;
import com.kanokna.shared.core.DomainException;
import com.kanokna.shared.money.Money;

/**
 * MODULE_CONTRACT id="MC-search-kafka-adapter" LAYER="adapters.in.kafka"
 * INTENT="Kafka listener consuming catalog events to sync search index"
 * LINKS="Technology.xml#TECH-kafka;DevelopmentPlan.xml#Flow-Event-Driven"
 *
 * Kafka listener for catalog events used to keep the search index in sync.
 */
@Component
public class SearchCatalogEventListener {

    private static final Logger log = LoggerFactory.getLogger(SearchCatalogEventListener.class);

    private final IndexProductUseCase indexProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;

    public SearchCatalogEventListener(
            IndexProductUseCase indexProductUseCase,
            DeleteProductUseCase deleteProductUseCase
    ) {
        this.indexProductUseCase = indexProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
    }

    @KafkaListener(
            topics = "${kafka.topics.product-published}",
            containerFactory = "productPublishedKafkaListenerContainerFactory"
    )
    public void onProductPublished(
            ProductTemplatePublishedEvent event,
            Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        if (event == null) {
            log.warn("Received null ProductTemplatePublishedEvent from topic={}", topic);
            acknowledgment.acknowledge();
            return;
        }
        CatalogProductEvent payload = new CatalogProductEvent(
                extractEventId(event.getMetadata()),
                "PRODUCT_TEMPLATE_PUBLISHED",
                event.getProductTemplateId(),
                event.getName(),
                event.getDescription(),
                event.getProductFamily(),
                event.getProfileSystem(),
                event.getOpeningTypesList(),
                event.getMaterialsList(),
                event.getColorsList(),
                toMoney(event.getBasePrice()),
                toMoney(event.getMaxPrice()),
                mapStatus(event.getStatus()),
                event.getThumbnailUrl(),
                event.getPopularity(),
                event.getOptionGroupCount(),
                toInstantIfPresent(event.getPublishedAt()),
                null
        );
        handleIndex(payload, acknowledgment, topic);
    }

    @KafkaListener(
            topics = "${kafka.topics.product-updated}",
            containerFactory = "productUpdatedKafkaListenerContainerFactory"
    )
    public void onProductUpdated(
            ProductTemplateUpdatedEvent event,
            Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        if (event == null) {
            log.warn("Received null ProductTemplateUpdatedEvent from topic={}", topic);
            acknowledgment.acknowledge();
            return;
        }
        CatalogProductEvent payload = new CatalogProductEvent(
                extractEventId(event.getMetadata()),
                "PRODUCT_TEMPLATE_UPDATED",
                event.getProductTemplateId(),
                event.getName(),
                event.getDescription(),
                event.getProductFamily(),
                event.getProfileSystem(),
                event.getOpeningTypesList(),
                event.getMaterialsList(),
                event.getColorsList(),
                toMoney(event.getBasePrice()),
                toMoney(event.getMaxPrice()),
                mapStatus(event.getStatus()),
                event.getThumbnailUrl(),
                event.getPopularity(),
                event.getOptionGroupCount(),
                null,
                toInstantIfPresent(event.getUpdatedAt())
        );
        handleIndex(payload, acknowledgment, topic);
    }

    @KafkaListener(
            topics = "${kafka.topics.product-unpublished}",
            containerFactory = "productUnpublishedKafkaListenerContainerFactory"
    )
    public void onProductUnpublished(
            ProductTemplateUnpublishedEvent event,
            Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        if (event == null) {
            log.warn("Received null ProductTemplateUnpublishedEvent from topic={}", topic);
            acknowledgment.acknowledge();
            return;
        }
        CatalogProductDeleteEvent payload = new CatalogProductDeleteEvent(
                extractEventId(event.getMetadata()),
                event.getProductTemplateId()
        );
        handleDelete(payload, acknowledgment, topic);
    }

    private void handleIndex(
            CatalogProductEvent payload,
            Acknowledgment acknowledgment,
            String topic
    ) {
        try {
            indexProductUseCase.indexProduct(payload);
            acknowledgment.acknowledge();
        } catch (DomainException ex) {
            if ("ERR-INDEX-INVALID-EVENT".equals(ex.getCode())) {
                log.warn("Skipping invalid catalog event from topic={}, reason={}", topic, ex.getMessage());
                acknowledgment.acknowledge();
                return;
            }
            throw ex;
        } catch (IllegalArgumentException ex) {
            log.warn("Skipping invalid catalog event from topic={}, reason={}", topic, ex.getMessage());
            acknowledgment.acknowledge();
        }
    }

    private void handleDelete(
            CatalogProductDeleteEvent payload,
            Acknowledgment acknowledgment,
            String topic
    ) {
        try {
            deleteProductUseCase.deleteProduct(payload);
            acknowledgment.acknowledge();
        } catch (DomainException ex) {
            if ("ERR-SEARCH-PRODUCT-ID-REQUIRED".equals(ex.getCode())) {
                log.warn("Skipping invalid delete event from topic={}, reason={}", topic, ex.getMessage());
                acknowledgment.acknowledge();
                return;
            }
            throw ex;
        } catch (IllegalArgumentException ex) {
            log.warn("Skipping invalid delete event from topic={}, reason={}", topic, ex.getMessage());
            acknowledgment.acknowledge();
        }
    }

    private String extractEventId(EventMetadata metadata) {
        if (metadata == null || metadata.getEventId().isBlank()) {
            return "unknown";
        }
        return metadata.getEventId();
    }

    private Money toMoney(com.kanokna.common.v1.Money money) {
        if (money == null || money.getCurrency() == Currency.CURRENCY_UNSPECIFIED) {
            return null;
        }
        return Money.ofMinor(money.getAmountMinor(), mapCurrency(money.getCurrency()));
    }

    private com.kanokna.shared.money.Currency mapCurrency(Currency currency) {
        return switch (currency) {
            case CURRENCY_EUR ->
                com.kanokna.shared.money.Currency.EUR;
            case CURRENCY_USD ->
                com.kanokna.shared.money.Currency.USD;
            case CURRENCY_RUB ->
                com.kanokna.shared.money.Currency.RUB;
            case CURRENCY_UNSPECIFIED, UNRECOGNIZED ->
                com.kanokna.shared.money.Currency.RUB;
        };
    }

    private ProductStatus mapStatus(com.kanokna.catalog.v1.ProductStatus status) {
        if (status == null) {
            return ProductStatus.UNSPECIFIED;
        }
        return switch (status) {
            case PRODUCT_STATUS_ACTIVE ->
                ProductStatus.ACTIVE;
            case PRODUCT_STATUS_DRAFT ->
                ProductStatus.DRAFT;
            case PRODUCT_STATUS_ARCHIVED ->
                ProductStatus.ARCHIVED;
            case PRODUCT_STATUS_UNSPECIFIED, UNRECOGNIZED ->
                ProductStatus.UNSPECIFIED;
        };
    }

    private Instant toInstantIfPresent(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        if (timestamp.getSeconds() == 0 && timestamp.getNanos() == 0) {
            return null;
        }
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
