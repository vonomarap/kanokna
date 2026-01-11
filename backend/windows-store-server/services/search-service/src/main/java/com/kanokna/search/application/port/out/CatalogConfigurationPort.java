package com.kanokna.search.application.port.out;

import com.kanokna.search.application.dto.CatalogProductPage;

/**
 * Outbound port for catalog product templates fetch.
 */
public interface CatalogConfigurationPort {
    CatalogProductPage listProductTemplates(int pageSize, String pageToken);
}
