package com.kanokna.catalog_configuration_service.adapters.out.memory;

import com.kanokna.catalog_configuration_service.application.port.out.SearchIndexPort;
import com.kanokna.catalog_configuration_service.domain.model.ProductTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NoOpSearchIndexPort implements SearchIndexPort {

    private static final Logger logger = LoggerFactory.getLogger(NoOpSearchIndexPort.class);

    @Override
    public void indexTemplate(ProductTemplate template) {
        logger.debug("[SEARCH][noop] index templateId={}", template.id().value());
    }
}
