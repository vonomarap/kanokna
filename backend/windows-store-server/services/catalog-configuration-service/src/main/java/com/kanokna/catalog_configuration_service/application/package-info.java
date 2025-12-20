/* <MODULE_MAP id="catalog-configuration-service.application">
   <PortsIn>
     <Port name="TemplateCommandPort" operations="defineTemplate,updateTemplateOptions,defineRules,publishCatalogVersion" />
     <Port name="QueryPort" operations="listTemplates,getTemplate,queryOptions,validateConfiguration,resolveBom" />
   </PortsIn>
   <PortsOut>
     <Port name="ProductTemplateRepository" />
     <Port name="BomTemplateRepository" />
     <Port name="RuleRepository" />
     <Port name="CertificationRepository" />
     <Port name="I18nPort" />
     <Port name="OutboxPublisher" />
     <Port name="SearchIndexPort" />
     <Port name="MediaMetadataPort" />
   </PortsOut>
   <Services>
     <Service name="ConfigurationApplicationService" />
   </Services>
   <Links>
     <Link ref="backend/windows-store-server/services/catalog-configuration-service/docs/DevelopmentPlan.xml#Ports-Inbound" />
     <Link ref="backend/windows-store-server/services/catalog-configuration-service/docs/DevelopmentPlan.xml#Contracts-domain-validation" />
     <Link ref="backend/windows-store-server/services/catalog-configuration-service/docs/DevelopmentPlan.xml#Contracts-domain-bom" />
   </Links>
 </MODULE_MAP> */
package com.kanokna.catalog_configuration_service.application;
