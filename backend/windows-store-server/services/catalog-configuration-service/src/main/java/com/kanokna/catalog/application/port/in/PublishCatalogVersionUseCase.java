package com.kanokna.catalog.application.port.in;

import com.kanokna.catalog.application.dto.PublishCatalogVersionCommand;

import java.util.UUID;

/**
 * Inbound port: Publish a new catalog version (admin operation).
 */
public interface PublishCatalogVersionUseCase {

    UUID publish(PublishCatalogVersionCommand command);
}
