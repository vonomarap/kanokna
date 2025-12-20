package com.kanokna.catalog_configuration_service.adapters.out.memory;

import com.kanokna.catalog_configuration_service.application.port.out.BomTemplateRepository;
import com.kanokna.catalog_configuration_service.domain.model.BomTemplate;
import com.kanokna.shared.core.Id;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryBomTemplateRepository implements BomTemplateRepository {

    private final Map<String, List<BomTemplate>> store = new ConcurrentHashMap<>();

    @Override
    public List<BomTemplate> findByTemplate(Id templateId, Id tenantId) {
        return store.getOrDefault(key(templateId, tenantId), List.of());
    }

    public void save(Id templateId, Id tenantId, BomTemplate bomTemplate) {
        store.computeIfAbsent(key(templateId, tenantId), k -> new ArrayList<>()).add(bomTemplate);
    }

    private String key(Id templateId, Id tenantId) {
        return tenantId.value() + "|" + templateId.value();
    }
}
