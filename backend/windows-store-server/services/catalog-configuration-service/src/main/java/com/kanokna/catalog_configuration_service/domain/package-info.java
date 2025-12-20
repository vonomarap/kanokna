/* <MODULE_MAP id="catalog-configuration-service.domain">
   <Aggregates>
     <Aggregate name="ProductTemplate" />
     <Aggregate name="CatalogVersion" />
   </Aggregates>
   <ValueObjects>
     <ValueObject name="ConfigurationSelection" />
     <ValueObject name="ConfigurationRuleSet" />
     <ValueObject name="ValidationResult" />
     <ValueObject name="BillOfMaterials" />
   </ValueObjects>
   <DomainServices>
     <Service name="ConfigurationValidationService" />
     <Service name="BomResolutionService" />
   </DomainServices>
   <Links>
     <Link ref="docs/DevelopmentPlan.xml#Contracts-domain-validation" />
     <Link ref="docs/DevelopmentPlan.xml#Contracts-domain-bom" />
   </Links>
 </MODULE_MAP> */
package com.kanokna.catalog_configuration_service.domain;
