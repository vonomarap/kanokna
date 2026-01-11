package com.kanokna.catalog.adapters.out.persistence;

import com.kanokna.catalog.application.port.out.ProductTemplateRepository;
import com.kanokna.catalog.domain.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing ProductTemplateRepository port.
 * Bridges domain model and JPA persistence.
 */
@Component
public class ProductTemplateRepositoryAdapter implements ProductTemplateRepository {

    private final ProductTemplateJpaRepository jpaRepository;

    public ProductTemplateRepositoryAdapter(ProductTemplateJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ProductTemplate save(ProductTemplate productTemplate) {
        ProductTemplateJpaEntity entity = mapToEntity(productTemplate);
        ProductTemplateJpaEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<ProductTemplate> findById(ProductTemplateId id) {
        return jpaRepository.findById(id.value())
            .map(this::mapToDomain);
    }

    @Override
    public List<ProductTemplate> findByStatus(TemplateStatus status) {
        return jpaRepository.findByStatus(mapStatusToJpa(status)).stream()
            .map(this::mapToDomain)
            .toList();
    }

    @Override
    public List<ProductTemplate> findByProductFamilyAndStatus(ProductFamily productFamily, TemplateStatus status) {
        return jpaRepository.findByProductFamilyAndStatus(mapFamilyToJpa(productFamily), mapStatusToJpa(status)).stream()
            .map(this::mapToDomain)
            .toList();
    }

    @Override
    public List<ProductTemplate> findAll() {
        return jpaRepository.findAll().stream()
            .map(this::mapToDomain)
            .toList();
    }

    @Override
    public boolean existsByNameAndProductFamily(String name, ProductFamily productFamily) {
        return jpaRepository.existsByNameAndProductFamily(name, mapFamilyToJpa(productFamily));
    }

    // Mapping methods (simplified - full implementation would be more comprehensive)
    private ProductTemplateJpaEntity mapToEntity(ProductTemplate domain) {
        ProductTemplateJpaEntity entity = new ProductTemplateJpaEntity();
        entity.setId(domain.getId().value());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setProductFamily(mapFamilyToJpa(domain.getProductFamily()));
        entity.setMinWidthCm(domain.getDimensionConstraints().minWidthCm());
        entity.setMaxWidthCm(domain.getDimensionConstraints().maxWidthCm());
        entity.setMinHeightCm(domain.getDimensionConstraints().minHeightCm());
        entity.setMaxHeightCm(domain.getDimensionConstraints().maxHeightCm());
        entity.setStatus(mapStatusToJpa(domain.getStatus()));
        entity.setVersion(domain.getVersion());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private ProductTemplate mapToDomain(ProductTemplateJpaEntity entity) {
        DimensionConstraints constraints = new DimensionConstraints(
            entity.getMinWidthCm(),
            entity.getMaxWidthCm(),
            entity.getMinHeightCm(),
            entity.getMaxHeightCm()
        );

        return new ProductTemplate(
            ProductTemplateId.of(entity.getId()),
            entity.getName(),
            entity.getDescription(),
            mapFamilyToDomain(entity.getProductFamily()),
            constraints
        );
    }

    private ProductTemplateJpaEntity.ProductFamilyJpa mapFamilyToJpa(ProductFamily domain) {
        return ProductTemplateJpaEntity.ProductFamilyJpa.valueOf(domain.name());
    }

    private ProductFamily mapFamilyToDomain(ProductTemplateJpaEntity.ProductFamilyJpa jpa) {
        return ProductFamily.valueOf(jpa.name());
    }

    private ProductTemplateJpaEntity.TemplateStatusJpa mapStatusToJpa(TemplateStatus domain) {
        return ProductTemplateJpaEntity.TemplateStatusJpa.valueOf(domain.name());
    }
}
