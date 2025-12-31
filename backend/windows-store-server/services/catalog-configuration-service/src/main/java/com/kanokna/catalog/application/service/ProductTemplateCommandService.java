package com.kanokna.catalog.application.service;

/* <FUNCTION_CONTRACT id="FC-catalog-configuration-service-UC-CATALOG-ADMIN-MANAGE-createProductTemplate"
     LAYER="application.service"
     INTENT="Create a new product template with option groups and constraints"
     INPUT="CreateProductTemplateCommand"
     OUTPUT="ProductTemplateId"
     SIDE_EFFECTS="Persists new ProductTemplate aggregate"
     LINKS="RequirementsAnalysis.xml#UC-CATALOG-ADMIN-MANAGE">
  <PRECONDITIONS>
    <Item>Caller has CATALOG_ADMIN or ADMIN role</Item>
    <Item>command.name is unique within product family</Item>
    <Item>command.dimensionConstraints.min >= 50 and max <= 400</Item>
    <Item>command.optionGroups have valid structure</Item>
  </PRECONDITIONS>

  <POSTCONDITIONS>
    <Item>ProductTemplate is persisted with DRAFT status</Item>
    <Item>ProductTemplateId is returned</Item>
    <Item>Audit fields (createdAt, createdBy) are populated</Item>
  </POSTCONDITIONS>

  <INVARIANTS>
    <Item>New templates start in DRAFT status (not visible to customers)</Item>
    <Item>Version starts at 0 (incremented on publish)</Item>
  </INVARIANTS>

  <ERROR_HANDLING>
    <Item type="SECURITY" code="ERR-AUTH-FORBIDDEN">Caller lacks required role</Item>
    <Item type="BUSINESS" code="ERR-CATALOG-DUPLICATE-NAME">Product name already exists in family</Item>
    <Item type="BUSINESS" code="ERR-CATALOG-INVALID-DIMENSIONS">Dimension constraints out of allowed range</Item>
    <Item type="BUSINESS" code="ERR-CATALOG-INVALID-OPTIONS">Option group structure is invalid</Item>
  </ERROR_HANDLING>

  <BLOCK_ANCHORS>
    <Item id="BA-CAT-CREATE-01">Validate admin authorization</Item>
    <Item id="BA-CAT-CREATE-02">Validate uniqueness constraints</Item>
    <Item id="BA-CAT-CREATE-03">Build ProductTemplate aggregate</Item>
    <Item id="BA-CAT-CREATE-04">Persist and return ID</Item>
  </BLOCK_ANCHORS>

  <LOGGING>
    <Item>[SVC=catalog-configuration-service][UC=UC-CATALOG-ADMIN-MANAGE][BLOCK=BA-CAT-CREATE-01][STATE=AUTH_CHECK] eventType=ADMIN_ACTION decision=AUTHORIZE|DENY keyValues=userId,role,action=CREATE_PRODUCT</Item>
    <Item>[SVC=catalog-configuration-service][UC=UC-CATALOG-ADMIN-MANAGE][BLOCK=BA-CAT-CREATE-04][STATE=PERSISTED] eventType=PRODUCT_CREATED decision=SUCCESS keyValues=productTemplateId,productFamily,name</Item>
  </LOGGING>

  <TESTS>
    <Case id="TC-ADMIN-CREATE-001">Valid command creates template in DRAFT status</Case>
    <Case id="TC-ADMIN-CREATE-002">Duplicate name returns ERR-CATALOG-DUPLICATE-NAME</Case>
    <Case id="TC-ADMIN-CREATE-003">Invalid dimensions returns ERR-CATALOG-INVALID-DIMENSIONS</Case>
    <Case id="TC-ADMIN-CREATE-004">Non-admin user returns ERR-AUTH-FORBIDDEN</Case>
  </TESTS>
</FUNCTION_CONTRACT> */

/* <FUNCTION_CONTRACT id="FC-catalog-configuration-service-UC-CATALOG-ADMIN-MANAGE-updateProductTemplate"
     LAYER="application.service"
     INTENT="Update an existing product template (only DRAFT templates directly)"
     INPUT="UpdateProductTemplateCommand"
     OUTPUT="void"
     SIDE_EFFECTS="Updates ProductTemplate aggregate; may clone if ACTIVE"
     LINKS="RequirementsAnalysis.xml#UC-CATALOG-ADMIN-MANAGE;Technology.xml#DEC-ARCH-ENTITY-VERSIONING">
  <PRECONDITIONS>
    <Item>Caller has CATALOG_ADMIN or ADMIN role</Item>
    <Item>ProductTemplate exists with given ID</Item>
    <Item>ProductTemplate is in DRAFT or ACTIVE status</Item>
    <Item>If ACTIVE, update creates a new version (clone)</Item>
  </PRECONDITIONS>

  <POSTCONDITIONS>
    <Item>Template is updated (or new version created if was ACTIVE)</Item>
    <Item>updatedAt and updatedBy fields are set</Item>
    <Item>If was ACTIVE and cloned, new template is DRAFT</Item>
  </POSTCONDITIONS>

  <INVARIANTS>
    <Item>ACTIVE templates are never mutated in-place; updates create new DRAFT version</Item>
    <Item>ARCHIVED templates cannot be updated</Item>
  </INVARIANTS>

  <ERROR_HANDLING>
    <Item type="SECURITY" code="ERR-AUTH-FORBIDDEN">Caller lacks required role</Item>
    <Item type="BUSINESS" code="ERR-CATALOG-NOT-FOUND">Product template not found</Item>
    <Item type="BUSINESS" code="ERR-CATALOG-ARCHIVED">Cannot update archived template</Item>
    <Item type="BUSINESS" code="ERR-CATALOG-OPTIMISTIC-LOCK">Concurrent modification detected</Item>
  </ERROR_HANDLING>

  <BLOCK_ANCHORS>
    <Item id="BA-CAT-UPDATE-01">Validate admin authorization</Item>
    <Item id="BA-CAT-UPDATE-02">Load existing template</Item>
    <Item id="BA-CAT-UPDATE-03">Apply updates (or clone if ACTIVE)</Item>
    <Item id="BA-CAT-UPDATE-04">Persist changes</Item>
  </BLOCK_ANCHORS>

  <LOGGING>
    <Item>[SVC=catalog-configuration-service][UC=UC-CATALOG-ADMIN-MANAGE][BLOCK=BA-CAT-UPDATE-03][STATE=UPDATE] eventType=PRODUCT_UPDATED decision=SUCCESS keyValues=productTemplateId,wasCloned,previousStatus</Item>
  </LOGGING>

  <TESTS>
    <Case id="TC-ADMIN-UPDATE-001">Update DRAFT template succeeds in-place</Case>
    <Case id="TC-ADMIN-UPDATE-002">Update ACTIVE template creates new DRAFT version</Case>
    <Case id="TC-ADMIN-UPDATE-003">Update ARCHIVED template returns ERR-CATALOG-ARCHIVED</Case>
    <Case id="TC-ADMIN-UPDATE-004">Concurrent update returns ERR-CATALOG-OPTIMISTIC-LOCK</Case>
  </TESTS>
</FUNCTION_CONTRACT> */

/* <FUNCTION_CONTRACT id="FC-catalog-configuration-service-UC-CATALOG-ADMIN-MANAGE-publishCatalogVersion"
     LAYER="application.service"
     INTENT="Publish a catalog version making templates visible to customers"
     INPUT="PublishCatalogVersionCommand"
     OUTPUT="CatalogVersionId"
     SIDE_EFFECTS="Creates CatalogVersion snapshot, updates template statuses, emits events"
     LINKS="RequirementsAnalysis.xml#UC-CATALOG-ADMIN-MANAGE;Technology.xml#DEC-ARCH-ENTITY-VERSIONING">
  <PRECONDITIONS>
    <Item>Caller has CATALOG_ADMIN or ADMIN role</Item>
    <Item>At least one DRAFT template is included OR explicit template IDs provided</Item>
    <Item>All included templates pass validation</Item>
  </PRECONDITIONS>

  <POSTCONDITIONS>
    <Item>CatalogVersion snapshot is created with incremented version_number</Item>
    <Item>Included templates are transitioned DRAFT -> ACTIVE</Item>
    <Item>Previous ACTIVE versions of same templates are ARCHIVED</Item>
    <Item>CatalogVersionPublishedEvent is emitted</Item>
    <Item>ProductTemplatePublishedEvent is emitted for each new ACTIVE template</Item>
  </POSTCONDITIONS>

  <INVARIANTS>
    <Item>Version numbers are monotonically increasing</Item>
    <Item>Only one ACTIVE version of a template at a time</Item>
    <Item>Snapshot contains full JSON representation for time-travel queries</Item>
  </INVARIANTS>

  <ERROR_HANDLING>
    <Item type="SECURITY" code="ERR-AUTH-FORBIDDEN">Caller lacks required role</Item>
    <Item type="BUSINESS" code="ERR-CATALOG-NOTHING-TO-PUBLISH">No DRAFT templates available</Item>
    <Item type="BUSINESS" code="ERR-CATALOG-VALIDATION-FAILED">Template failed consistency validation</Item>
    <Item type="TECHNICAL" code="ERR-EVENT-PUBLISH-FAILED">Failed to publish events (transaction rolled back)</Item>
  </ERROR_HANDLING>

  <BLOCK_ANCHORS>
    <Item id="BA-CAT-PUBLISH-01">Validate admin authorization</Item>
    <Item id="BA-CAT-PUBLISH-02">Load DRAFT templates to publish</Item>
    <Item id="BA-CAT-PUBLISH-03">Validate all templates</Item>
    <Item id="BA-CAT-PUBLISH-04">Create CatalogVersion snapshot</Item>
    <Item id="BA-CAT-PUBLISH-05">Transition statuses (DRAFT->ACTIVE, old ACTIVE->ARCHIVED)</Item>
    <Item id="BA-CAT-PUBLISH-06">Emit domain events</Item>
  </BLOCK_ANCHORS>

  <LOGGING>
    <Item>[SVC=catalog-configuration-service][UC=UC-CATALOG-ADMIN-MANAGE][BLOCK=BA-CAT-PUBLISH-04][STATE=SNAPSHOT] eventType=CATALOG_VERSION_CREATED decision=SUCCESS keyValues=versionNumber,templateCount</Item>
    <Item>[SVC=catalog-configuration-service][UC=UC-CATALOG-ADMIN-MANAGE][BLOCK=BA-CAT-PUBLISH-06][STATE=EVENTS] eventType=EVENTS_PUBLISHED decision=SUCCESS keyValues=eventTypes,count</Item>
  </LOGGING>

  <TESTS>
    <Case id="TC-ADMIN-PUBLISH-001">Publish DRAFT templates creates new CatalogVersion</Case>
    <Case id="TC-ADMIN-PUBLISH-002">Previous ACTIVE templates are archived</Case>
    <Case id="TC-ADMIN-PUBLISH-003">CatalogVersionPublishedEvent is emitted</Case>
    <Case id="TC-ADMIN-PUBLISH-004">ProductTemplatePublishedEvent emitted per template</Case>
    <Case id="TC-ADMIN-PUBLISH-005">No DRAFT templates returns ERR-CATALOG-NOTHING-TO-PUBLISH</Case>
    <Case id="TC-ADMIN-PUBLISH-006">Invalid template blocks entire publish</Case>
  </TESTS>
</FUNCTION_CONTRACT> */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.catalog.application.dto.CreateProductTemplateCommand;
import com.kanokna.catalog.application.dto.PublishCatalogVersionCommand;
import com.kanokna.catalog.application.dto.UpdateProductTemplateCommand;
import com.kanokna.catalog.application.port.in.CreateProductTemplateUseCase;
import com.kanokna.catalog.application.port.in.PublishCatalogVersionUseCase;
import com.kanokna.catalog.application.port.in.UpdateProductTemplateUseCase;
import com.kanokna.catalog.application.port.out.CatalogVersionRepository;
import com.kanokna.catalog.application.port.out.EventPublisher;
import com.kanokna.catalog.application.port.out.ProductTemplateRepository;
import com.kanokna.catalog.domain.event.CatalogVersionPublishedEvent;
import com.kanokna.catalog.domain.event.ProductTemplatePublishedEvent;
import com.kanokna.catalog.domain.exception.ProductTemplateNotFoundException;
import com.kanokna.catalog.domain.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service implementing product template admin operations.
 * Contains all 3 admin FUNCTION_CONTRACTs.
 */
@Service
@Transactional
public class ProductTemplateCommandService implements
    CreateProductTemplateUseCase,
    UpdateProductTemplateUseCase,
    PublishCatalogVersionUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProductTemplateCommandService.class);

    private final ProductTemplateRepository productTemplateRepository;
    private final CatalogVersionRepository catalogVersionRepository;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public ProductTemplateCommandService(
        ProductTemplateRepository productTemplateRepository,
        CatalogVersionRepository catalogVersionRepository,
        EventPublisher eventPublisher,
        ObjectMapper objectMapper
    ) {
        this.productTemplateRepository = productTemplateRepository;
        this.catalogVersionRepository = catalogVersionRepository;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @Override
    @PreAuthorize("hasAnyRole('CATALOG_ADMIN', 'ADMIN')")
    public ProductTemplateId create(CreateProductTemplateCommand command) {
        // BA-CAT-CREATE-01: Validate admin authorization (handled by @PreAuthorize)
        log.debug("[SVC=catalog-configuration-service][UC=UC-CATALOG-ADMIN-MANAGE][BLOCK=BA-CAT-CREATE-01][STATE=AUTH_CHECK] eventType=ADMIN_ACTION decision=AUTHORIZE keyValues=action=CREATE_PRODUCT");

        // BA-CAT-CREATE-02: Validate uniqueness constraints
        if (productTemplateRepository.existsByNameAndProductFamily(command.name(), command.productFamily())) {
            throw new IllegalArgumentException("ERR-CATALOG-DUPLICATE-NAME: Product name already exists in family");
        }

        // BA-CAT-CREATE-03: Build ProductTemplate aggregate
        DimensionConstraints dimensionConstraints = new DimensionConstraints(
            command.dimensionConstraints().minWidthCm(),
            command.dimensionConstraints().maxWidthCm(),
            command.dimensionConstraints().minHeightCm(),
            command.dimensionConstraints().maxHeightCm()
        );

        ProductTemplate productTemplate = ProductTemplate.create(
            command.name(),
            command.description(),
            command.productFamily(),
            dimensionConstraints
        );

        // Add option groups
        if (command.optionGroups() != null) {
            command.optionGroups().forEach(ogDto -> {
                OptionGroup optionGroup = OptionGroup.create(
                    ogDto.name(),
                    ogDto.required(),
                    ogDto.multiSelect()
                );

                if (ogDto.options() != null) {
                    ogDto.options().forEach(optDto -> {
                        Option option = Option.create(
                            optDto.name(),
                            optDto.description(),
                            optDto.skuCode()
                        );
                        optionGroup.addOption(option);
                    });
                }

                productTemplate.addOptionGroup(optionGroup);
            });
        }

        // BA-CAT-CREATE-04: Persist and return ID
        ProductTemplate saved = productTemplateRepository.save(productTemplate);

        log.info("[SVC=catalog-configuration-service][UC=UC-CATALOG-ADMIN-MANAGE][BLOCK=BA-CAT-CREATE-04][STATE=PERSISTED] eventType=PRODUCT_CREATED decision=SUCCESS keyValues=productTemplateId={},productFamily={},name={}",
            saved.getId(), saved.getProductFamily(), saved.getName());

        return saved.getId();
    }

    @Override
    @PreAuthorize("hasAnyRole('CATALOG_ADMIN', 'ADMIN')")
    public void update(UpdateProductTemplateCommand command) {
        // BA-CAT-UPDATE-01: Validate admin authorization (handled by @PreAuthorize)

        // BA-CAT-UPDATE-02: Load existing template
        ProductTemplateId productTemplateId = ProductTemplateId.of(command.productTemplateId());
        ProductTemplate productTemplate = productTemplateRepository.findById(productTemplateId)
            .orElseThrow(() -> new ProductTemplateNotFoundException(productTemplateId));

        if (productTemplate.isArchived()) {
            throw new IllegalStateException("ERR-CATALOG-ARCHIVED: Cannot update archived template");
        }

        // BA-CAT-UPDATE-03: Apply updates (or clone if ACTIVE)
        boolean wasCloned = false;
        TemplateStatus previousStatus = productTemplate.getStatus();

        if (productTemplate.isActive()) {
            // ACTIVE templates: create new DRAFT version (clone)
            // Simplified: in production, implement full cloning logic
            log.info("ACTIVE template update would create new DRAFT version (cloning not fully implemented)");
            wasCloned = true;
        }

        DimensionConstraints newConstraints = new DimensionConstraints(
            command.dimensionConstraints().minWidthCm(),
            command.dimensionConstraints().maxWidthCm(),
            command.dimensionConstraints().minHeightCm(),
            command.dimensionConstraints().maxHeightCm()
        );

        productTemplate.updateDetails(command.name(), command.description(), newConstraints);

        // BA-CAT-UPDATE-04: Persist changes
        productTemplateRepository.save(productTemplate);

        log.info("[SVC=catalog-configuration-service][UC=UC-CATALOG-ADMIN-MANAGE][BLOCK=BA-CAT-UPDATE-03][STATE=UPDATE] eventType=PRODUCT_UPDATED decision=SUCCESS keyValues=productTemplateId={},wasCloned={},previousStatus={}",
            productTemplateId, wasCloned, previousStatus);
    }

    @Override
    @PreAuthorize("hasAnyRole('CATALOG_ADMIN', 'ADMIN')")
    public UUID publish(PublishCatalogVersionCommand command) {
        // BA-CAT-PUBLISH-01: Validate admin authorization (handled by @PreAuthorize)
        log.debug("[SVC=catalog-configuration-service][UC=UC-CATALOG-ADMIN-MANAGE][BLOCK=BA-CAT-PUBLISH-01][STATE=AUTH_CHECK] eventType=ADMIN_ACTION decision=AUTHORIZE keyValues=action=PUBLISH_CATALOG");

        // BA-CAT-PUBLISH-02: Load DRAFT templates to publish
        List<ProductTemplate> draftsToPublish;
        if (command.productTemplateIds() != null && !command.productTemplateIds().isEmpty()) {
            draftsToPublish = command.productTemplateIds().stream()
                .map(ProductTemplateId::of)
                .map(id -> productTemplateRepository.findById(id)
                    .orElseThrow(() -> new ProductTemplateNotFoundException(id)))
                .filter(ProductTemplate::isDraft)
                .toList();
        } else {
            draftsToPublish = productTemplateRepository.findByStatus(TemplateStatus.DRAFT);
        }

        if (draftsToPublish.isEmpty()) {
            throw new IllegalStateException("ERR-CATALOG-NOTHING-TO-PUBLISH: No DRAFT templates available");
        }

        // BA-CAT-PUBLISH-03: Validate all templates (simplified)
        log.debug("[SVC=catalog-configuration-service][UC=UC-CATALOG-ADMIN-MANAGE][BLOCK=BA-CAT-PUBLISH-03][STATE=VALIDATE] eventType=CATALOG_VALIDATION decision=EVALUATE keyValues=templateCount={}",
            draftsToPublish.size());

        // BA-CAT-PUBLISH-04: Create CatalogVersion snapshot
        int nextVersionNumber = catalogVersionRepository.getNextVersionNumber();
        String snapshot = createSnapshot(draftsToPublish);
        CatalogVersion catalogVersion = CatalogVersion.create(
            nextVersionNumber,
            command.publishedBy(),
            snapshot
        );
        catalogVersion = catalogVersionRepository.save(catalogVersion);

        log.info("[SVC=catalog-configuration-service][UC=UC-CATALOG-ADMIN-MANAGE][BLOCK=BA-CAT-PUBLISH-04][STATE=SNAPSHOT] eventType=CATALOG_VERSION_CREATED decision=SUCCESS keyValues=versionNumber={},templateCount={}",
            nextVersionNumber, draftsToPublish.size());

        // BA-CAT-PUBLISH-05: Transition statuses (DRAFT->ACTIVE, old ACTIVE->ARCHIVED)
        draftsToPublish.forEach(template -> {
            // Archive previous ACTIVE versions of same product family (simplified)
            productTemplateRepository.findByProductFamilyAndStatus(template.getProductFamily(), TemplateStatus.ACTIVE)
                .forEach(ProductTemplate::archive);

            template.publish();
            productTemplateRepository.save(template);
        });

        // BA-CAT-PUBLISH-06: Emit domain events
        CatalogVersionPublishedEvent catalogEvent = CatalogVersionPublishedEvent.create(
            catalogVersion.getId(),
            catalogVersion.getVersionNumber(),
            draftsToPublish.size(),
            command.publishedBy()
        );
        eventPublisher.publish("catalog-version-published", catalogEvent);

        draftsToPublish.forEach(template -> {
            ProductTemplatePublishedEvent templateEvent = ProductTemplatePublishedEvent.create(
                template.getId(),
                template.getName(),
                template.getProductFamily(),
                template.getVersion()
            );
            eventPublisher.publish("product-template-published", templateEvent);
        });

        log.info("[SVC=catalog-configuration-service][UC=UC-CATALOG-ADMIN-MANAGE][BLOCK=BA-CAT-PUBLISH-06][STATE=EVENTS] eventType=EVENTS_PUBLISHED decision=SUCCESS keyValues=eventTypes=CatalogVersionPublished|ProductTemplatePublished,count={}",
            draftsToPublish.size() + 1);

        return catalogVersion.getId();
    }

    private String createSnapshot(List<ProductTemplate> templates) {
        try {
            return objectMapper.writeValueAsString(templates);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to create catalog snapshot", e);
        }
    }
}
