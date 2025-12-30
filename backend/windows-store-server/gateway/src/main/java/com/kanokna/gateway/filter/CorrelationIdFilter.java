/* <FUNCTION_CONTRACT id="FC-gateway-filter-CorrelationIdFilter-filter"
     LAYER="filter"
     INTENT="Inject or propagate X-Correlation-ID header for distributed tracing"
     INPUT="ServerWebExchange (incoming request)"
     OUTPUT="Mono<Void> (modified exchange with correlation ID)"
     SIDE_EFFECTS="Adds correlation ID to MDC for logging, modifies request headers"
     LINKS="RequirementsAnalysis.xml#NFR-OBS-TRACING;Technology.xml#TECH-opentelemetry">
  <PRECONDITIONS>
    <Item>ServerWebExchange is not null</Item>
    <Item>GatewayFilterChain is not null</Item>
  </PRECONDITIONS>

  <POSTCONDITIONS>
    <Item>X-Correlation-ID header present on request to downstream service</Item>
    <Item>X-Correlation-ID header present on response to client</Item>
    <Item>Correlation ID available in MDC for log statements</Item>
  </POSTCONDITIONS>

  <INVARIANTS>
    <Item>Correlation ID is a valid UUID v4 string</Item>
    <Item>If incoming request has X-Correlation-ID, it is preserved (not replaced)</Item>
    <Item>Filter executes on every request (ordered before routing)</Item>
  </INVARIANTS>

  <ERROR_HANDLING>
    <Item type="TECHNICAL" code="N/A">Filter chain exceptions propagate; correlation ID still logged</Item>
  </ERROR_HANDLING>

  <BLOCK_ANCHORS>
    <Item id="BA-GW-CORR-01">Extract or generate correlation ID</Item>
    <Item id="BA-GW-CORR-02">Set MDC context</Item>
    <Item id="BA-GW-CORR-03">Mutate request with correlation ID header</Item>
    <Item id="BA-GW-CORR-04">Add correlation ID to response headers</Item>
  </BLOCK_ANCHORS>

  <LOGGING>
    <Item>[SVC=gateway][UC=TRACING][BLOCK=BA-GW-CORR-01][STATE=EXTRACT_OR_GENERATE] eventType=CORRELATION_ID decision=EXTRACTED|GENERATED keyValues=correlationId</Item>
  </LOGGING>

  <TESTS>
    <Case id="TC-CORR-001">Request without X-Correlation-ID gets new UUID generated</Case>
    <Case id="TC-CORR-002">Request with X-Correlation-ID preserves existing value</Case>
    <Case id="TC-CORR-003">Response includes X-Correlation-ID header</Case>
    <Case id="TC-CORR-004">Logs include correlation ID in MDC</Case>
  </TESTS>
</FUNCTION_CONTRACT> */
package com.kanokna.gateway.filter;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final String HEADER_NAME = "X-Correlation-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // <BLOCK_ANCHOR id="BA-GW-CORR-01">Extract or generate correlation ID</BLOCK_ANCHOR>
        String incoming = exchange.getRequest().getHeaders().getFirst(HEADER_NAME);
        boolean extracted = isValidUuidV4(incoming);
        String correlationId = extracted ? incoming : UUID.randomUUID().toString();

        logger.info(
            "[SVC=gateway][UC=TRACING][BLOCK=BA-GW-CORR-01][STATE=EXTRACT_OR_GENERATE] " +
            "eventType=CORRELATION_ID decision={} keyValues=correlationId={}",
            extracted ? "EXTRACTED" : "GENERATED",
            correlationId
        );

        // <BLOCK_ANCHOR id="BA-GW-CORR-02">Set MDC context</BLOCK_ANCHOR>
        MDC.put("correlationId", correlationId);

        // <BLOCK_ANCHOR id="BA-GW-CORR-03">Mutate request with correlation ID header</BLOCK_ANCHOR>
        ServerHttpRequest request = exchange.getRequest().mutate().header(HEADER_NAME, correlationId).build();

        // <BLOCK_ANCHOR id="BA-GW-CORR-04">Add correlation ID to response headers</BLOCK_ANCHOR>
        exchange.getResponse().getHeaders().set(HEADER_NAME, correlationId);

        return chain.filter(exchange.mutate().request(request).build())
            .doFinally(signal -> MDC.remove("correlationId"));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private boolean isValidUuidV4(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            UUID uuid = UUID.fromString(value);
            return uuid.version() == 4;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
