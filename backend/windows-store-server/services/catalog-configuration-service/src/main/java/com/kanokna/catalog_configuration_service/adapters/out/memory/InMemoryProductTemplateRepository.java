package com.kanokna.catalog_configuration_service.adapters.out.memory;

import com.kanokna.catalog_configuration_service.application.port.out.ProductTemplateRepository;
import com.kanokna.catalog_configuration_service.domain.model.ProductTemplate;
import com.kanokna.shared.core.Id;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryProductTemplateRepository implements ProductTemplateRepository {

    private final Map<String, ProductTemplate> store = new ConcurrentHashMap<>();

    @Override
    public Optional<ProductTemplate> findActiveById(Id templateId, Id tenantId) {
        return Optional.ofNullable(store.get(key(templateId, tenantId)))
            .filter(ProductTemplate::isActive);
    }

    @Override
    public List<ProductTemplate> findActiveByTenant(Id tenantId) {
        return store.values().stream()
            .filter(ProductTemplate::isActive)
            .filter(template -> template.tenantId().equals(tenantId))
            .toList();
    }

    @Override
    public void save(ProductTemplate template) {
        store.put(key(template.id(), template.tenantId()), template);
    }

    private String key(Id templateId, Id tenantId) {
        return tenantId.value() + "|" + templateId.value();
    }
}
