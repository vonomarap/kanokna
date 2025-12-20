/* <MODULE_MAP id="pricing-service.domain">
   <Aggregates>
     <Aggregate name="PriceBook" />
     <Aggregate name="Campaign" />
   </Aggregates>
   <ValueObjects>
     <ValueObject name="QuoteRequest" />
     <ValueObject name="Quote" />
     <ValueObject name="TaxRule" />
     <ValueObject name="OptionPremiumKey" />
   </ValueObjects>
   <DomainServices>
     <Service name="PriceCalculationService" />
   </DomainServices>
   <Events>
     <Event name="QuoteCalculatedEvent" />
   </Events>
   <Links>
     <Link ref="backend/windows-store-server/services/pricing-service/docs/DevelopmentPlan.xml#Contracts-calc" />
     <Link ref="backend/windows-store-server/services/pricing-service/docs/RequirementsAnalysis.xml#UC-PRICE-PREVIEW" />
   </Links>
 </MODULE_MAP> */
package com.kanokna.pricing_service.domain;
