package com.kanokna.catalog_configuration_service.domain.service;

import com.kanokna.catalog_configuration_service.domain.event.BomResolvedEvent;
import com.kanokna.catalog_configuration_service.domain.exception.CatalogDomainException;
import com.kanokna.catalog_configuration_service.domain.model.BomTemplate;
import com.kanokna.catalog_configuration_service.domain.model.ConfigurationSelection;
import com.kanokna.catalog_configuration_service.domain.model.ProductTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/* <MODULE_CONTRACT id="mod.catalog.domain.bom"
     ROLE="Domain service for BOM resolution"
     SERVICE="catalog-configuration-service" LAYER="domain.service"
     BOUNDED_CONTEXT="catalog-configuration"
     LINKS="backend/windows-store-server/services/catalog-configuration-service/docs/RequirementsAnalysis.xml#UC-RESOLVE-BOM,backend/windows-store-server/services/catalog-configuration-service/docs/DevelopmentPlan.xml#Contracts-domain-bom">
   <PURPOSE>
     Map validated configurations to BOM templates and produce deterministic component lists for downstream fulfillment.
   </PURPOSE>
   <RESPONSIBILITIES>
     <Item>Select matching BOM template based on option matchers.</Item>
     <Item>Merge duplicate BOM items deterministically.</Item>
     <Item>Emit BomResolvedEvent with chosen template and items.</Item>
   </RESPONSIBILITIES>
   <LOGGING>
     <Pattern>[CFG][resolveBom][block={id}][state={state}]</Pattern>
     <Anchors>
       <Anchor id="BOM-RES-SELECT" purpose="Select BOM template by option matchers"/>
       <Anchor id="BOM-RES-MAP" purpose="Merge BOM items"/>
       <Anchor id="BOM-RES-EVENT" purpose="Build BomResolvedEvent"/>
     </Anchors>
   </LOGGING>
   <TESTS>
     <Case id="TC-BOM-001">Matching BOM template returns merged items and event.</Case>
     <Case id="TC-BOM-002">No matching template throws CatalogDomainException.</Case>
     <Case id="TC-BOM-003">Duplicate SKU quantities are merged.</Case>
   </TESTS>
 </MODULE_CONTRACT> */
public final class BomResolutionService {

    /* <FUNCTION_CONTRACT id="resolveBom"
         LAYER="domain.service"
         INTENT="Resolve BOM items for a validated configuration"
         INPUT="ProductTemplate template, ConfigurationSelection selection, List<BomTemplate> bomTemplates"
         OUTPUT="BomTemplate.BomResolutionResult"
         SIDE_EFFECTS="None; returns BomResolvedEvent for adapters to publish."
         LINKS="backend/windows-store-server/services/catalog-configuration-service/docs/RequirementsAnalysis.xml#UC-RESOLVE-BOM;backend/windows-store-server/services/catalog-configuration-service/docs/DevelopmentPlan.xml#Contracts-domain-bom">
       <PRECONDITIONS>
         <Item>selection has been validated; template and selection share ids.</Item>
         <Item>bomTemplates list is non-null.</Item>
       </PRECONDITIONS>
       <POSTCONDITIONS>
         <Item>Returned result contains merged BOM items without duplicates.</Item>
         <Item>BomResolvedEvent references templateId, tenantId, and catalogVersion.</Item>
       </POSTCONDITIONS>
       <INVARIANTS>
         <Item>No domain mutation; selection and templates remain immutable.</Item>
       </INVARIANTS>
       <ERROR_HANDLING>
         - Throws CatalogDomainException when no BOM template matches the selection.
       </ERROR_HANDLING>
       <LOGGING>
         - Decision traces per block anchor for adapters to log with pattern [CFG][resolveBom][block=ID][state=STATE].
       </LOGGING>
     </FUNCTION_CONTRACT> */
    public BomTemplate.BomResolutionResult resolveBom(ProductTemplate template, ConfigurationSelection selection, List<BomTemplate> bomTemplates) {
        Objects.requireNonNull(template, "template");
        Objects.requireNonNull(selection, "selection");
        Objects.requireNonNull(bomTemplates, "bomTemplates");

        if (!template.id().equals(selection.templateId())) {
            throw new CatalogDomainException("Selection template mismatch: " + selection.templateId());
        }

        /* <BLOCK_ANCHOR id="BOM-RES-SELECT" purpose="Select BOM template by option matchers"/> */
        BomTemplate matchedTemplate = bomTemplates.stream()
            .filter(templateCandidate -> templateCandidate.matches(selection))
            .findFirst()
            .orElseThrow(() -> new CatalogDomainException("No BOM template matched selection signature " + selection.signatureKey()));

        /* <BLOCK_ANCHOR id="BOM-RES-MAP" purpose="Merge BOM items"/> */
        List<BomTemplate.BomItem> resolvedItems = new ArrayList<>(matchedTemplate.items());

        /* <BLOCK_ANCHOR id="BOM-RES-EVENT" purpose="Build BomResolvedEvent"/> */
        BomResolvedEvent event = BomResolvedEvent.of(
            selection.templateId(),
            selection.tenantId(),
            selection.catalogVersion(),
            matchedTemplate.code(),
            resolvedItems
        );

        return BomTemplate.BomResolutionResult.merged(resolvedItems, event);
    }
}
