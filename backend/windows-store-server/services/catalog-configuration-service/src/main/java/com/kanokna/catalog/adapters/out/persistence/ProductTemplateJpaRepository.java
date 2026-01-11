package com.kanokna.catalog.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for ProductTemplate.
 */
@Repository
public interface ProductTemplateJpaRepository extends JpaRepository<ProductTemplateJpaEntity, UUID> {

    List<ProductTemplateJpaEntity> findByStatus(ProductTemplateJpaEntity.TemplateStatusJpa status);

    List<ProductTemplateJpaEntity> findByProductFamilyAndStatus(
        ProductTemplateJpaEntity.ProductFamilyJpa productFamily,
        ProductTemplateJpaEntity.TemplateStatusJpa status
    );

    boolean existsByNameAndProductFamily(
        String name,
        ProductTemplateJpaEntity.ProductFamilyJpa productFamily
    );
}
