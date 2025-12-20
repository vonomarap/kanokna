/* <MODULE_MAP id="order-service.application">
   <PortsIn>
     <Port name="CheckoutPort" operations="placeOrder" />
     <Port name="PaymentPort" operations="initiatePayment,processPaymentCallback" />
     <Port name="OrderQueryPort" operations="getOrder" />
     <Port name="InstallationPort" operations="scheduleInstallation" />
   </PortsIn>
   <PortsOut>
     <Port name="OrderRepository" />
     <Port name="PaymentRepository" />
     <Port name="PaymentGatewayPort" />
     <Port name="NotificationPublisher" />
     <Port name="OutboxPublisher" />
     <Port name="CartPort" />
     <Port name="PricingPort" />
   </PortsOut>
   <Services>
     <Service name="OrderApplicationService" />
   </Services>
   <Links>
     <Link ref="backend/windows-store-server/services/order-service/docs/DevelopmentPlan.xml#Flow-Checkout" />
     <Link ref="backend/windows-store-server/services/order-service/docs/DevelopmentPlan.xml#Flow-Payment" />
   </Links>
 </MODULE_MAP> */
package com.kanokna.order_service.application;
