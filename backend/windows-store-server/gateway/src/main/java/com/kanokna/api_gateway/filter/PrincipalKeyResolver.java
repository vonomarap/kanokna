package com.kanokna.api_gateway.filter;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PrincipalKeyResolver implements KeyResolver {
    @Override
    public Mono<String> resolve(org.springframework.web.server.ServerWebExchange exchange) {
        return exchange.getPrincipal()
            .map(p -> "principal:" + p.getName())
            .switchIfEmpty(Mono.just("anon:" + exchange.getRequest().getRemoteAddress()));
    }
}
