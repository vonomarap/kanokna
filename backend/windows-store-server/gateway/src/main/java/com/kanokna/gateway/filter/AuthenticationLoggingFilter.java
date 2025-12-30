package com.kanokna.gateway.filter;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.SecurityWebFiltersOrder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

@Component
public class AuthenticationLoggingFilter implements WebFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationLoggingFilter.class);
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    private static final List<String> ADMIN_PATHS = List.of(
        "/api/reports/**",
        "/api/catalog/admin/**",
        "/api/pricing/admin/**",
        "/api/accounts/admin/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
            .then(Mono.defer(() -> exchange.getPrincipal()
                .cast(Authentication.class)
                .defaultIfEmpty(null)
                .doOnNext(auth -> logAuth(exchange, auth))
                .then()));
    }

    @Override
    public int getOrder() {
        return SecurityWebFiltersOrder.AUTHORIZATION.getOrder() + 1;
    }

    private void logAuth(ServerWebExchange exchange, Authentication auth) {
        String path = exchange.getRequest().getURI().getPath();
        boolean adminPath = ADMIN_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
        String requiredRole = adminPath ? "ADMIN" : "NONE";

        // <BLOCK_ANCHOR id="BA-GW-AUTH-02">Extract roles from JWT claims</BLOCK_ANCHOR>
        List<String> roles = auth == null
            ? List.of()
            : auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        String userId = auth == null ? "anonymous" : auth.getName();

        HttpStatus status = exchange.getResponse().getStatusCode();
        String decision = (status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN) ? "DENY" : "ALLOW";

        logger.info(
            "[SVC=gateway][UC=AUTH][BLOCK=BA-GW-AUTH-01][STATE=TOKEN_VALIDATED] " +
            "eventType=AUTH_CHECK decision={} keyValues=userId={},roles={},path={}",
            decision,
            userId,
            roles,
            path
        );

        logger.info(
            "[SVC=gateway][UC=AUTH][BLOCK=BA-GW-AUTH-03][STATE=AUTHORIZATION] " +
            "eventType=AUTHZ_CHECK decision={} keyValues=requiredRole={},userRoles={},path={}",
            decision,
            requiredRole,
            roles,
            path
        );
    }
}
