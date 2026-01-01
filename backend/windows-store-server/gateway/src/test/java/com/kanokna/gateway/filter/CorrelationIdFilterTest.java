package com.kanokna.gateway.filter;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {
    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void generatesCorrelationIdWhenMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/orders/test").build()
        );
        CapturingChain chain = new CapturingChain();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        String correlationId = chain.exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
        assertThat(correlationId).isNotBlank();
        assertThat(isValidUuidV4(correlationId)).isTrue();
        assertThat(chain.exchange.getResponse().getHeaders().getFirst("X-Correlation-ID"))
            .isEqualTo(correlationId);
        assertThat(chain.mdcValue).isEqualTo(correlationId);
    }

    @Test
    void preservesCorrelationIdWhenPresent() {
        String existing = UUID.randomUUID().toString();
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/orders/test").header("X-Correlation-ID", existing).build()
        );
        CapturingChain chain = new CapturingChain();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        String correlationId = chain.exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
        assertThat(correlationId).isEqualTo(existing);
        assertThat(chain.exchange.getResponse().getHeaders().getFirst("X-Correlation-ID"))
            .isEqualTo(existing);
        assertThat(chain.mdcValue).isEqualTo(existing);
    }

    private boolean isValidUuidV4(String value) {
        UUID uuid = UUID.fromString(value);
        return uuid.version() == 4;
    }

    private static final class CapturingChain implements GatewayFilterChain {
        private ServerWebExchange exchange;
        private String mdcValue;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            this.exchange = exchange;
            this.mdcValue = MDC.get("correlationId");
            MockServerHttpResponse response = (MockServerHttpResponse) exchange.getResponse();
            response.setComplete();
            return Mono.empty();
        }
    }
}
