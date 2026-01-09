package com.kanokna.search.application.port.in;

import com.kanokna.search.application.dto.ReindexCommand;
import com.kanokna.search.application.dto.ReindexResult;

/**
 * Inbound port for catalog reindex.
 */
public interface ReindexCatalogUseCase {
    ReindexResult reindexCatalog(ReindexCommand command);
}
