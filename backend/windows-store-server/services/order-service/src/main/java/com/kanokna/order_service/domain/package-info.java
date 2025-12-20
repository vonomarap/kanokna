/* <MODULE_MAP id="order-service.domain">
   <Aggregates>
     <Aggregate name="Order" />
     <Aggregate name="Payment" />
   </Aggregates>
   <ValueObjects>
     <ValueObject name="OrderItem" />
     <ValueObject name="Totals" />
     <ValueObject name="ShippingInfo" />
     <ValueObject name="InstallationInfo" />
     <ValueObject name="DecisionTrace" />
   </ValueObjects>
   <DomainServices>
     <Service name="OrderDomainService" />
   </DomainServices>
   <Events>
     <Event name="OrderCreatedEvent" />
     <Event name="OrderConfirmedEvent" />
     <Event name="PaymentAppliedEvent" />
   </Events>
   <Links>
     <Link ref="backend/windows-store-server/services/order-service/docs/DevelopmentPlan.xml#Flow-Checkout" />
     <Link ref="backend/windows-store-server/services/order-service/docs/DevelopmentPlan.xml#Flow-Payment" />
   </Links>
 </MODULE_MAP> */
package com.kanokna.order_service.domain;
