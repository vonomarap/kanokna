/* <MODULE_MAP id="pricing-service.application">
   <PortsIn>
     <Port name="QuotePort" operations="quoteConfiguration,quoteCart" />
     <Port name="PriceAdminPort" operations="createOrUpdatePriceBook,publishPriceBook,defineCampaign,updateTaxRules" />
   </PortsIn>
   <PortsOut>
     <Port name="PriceBookRepository" />
     <Port name="CampaignRepository" />
     <Port name="TaxRuleRepository" />
     <Port name="QuoteCache" />
     <Port name="FxRatePort" />
     <Port name="OutboxPublisher" />
   </PortsOut>
   <Services>
     <Service name="PriceApplicationService" />
   </Services>
   <Links>
     <Link ref="backend/windows-store-server/services/pricing-service/docs/DevelopmentPlan.xml#Ports" />
     <Link ref="backend/windows-store-server/services/pricing-service/docs/DevelopmentPlan.xml#Flow-Quote" />
   </Links>
 </MODULE_MAP> */
package com.kanokna.pricing_service.application;
