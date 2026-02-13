package com.kanokna.gateway.config;

import java.util.List;

/**
 * Centralized path constants for gateway security and logging configuration.
 * Keeping these in one place ensures SecurityConfig and
 * AuthenticationLoggingFilter never go out of sync.
 */
public final class GatewayPaths {

    public static final String[] PUBLIC_PATHS = {
        "/actuator/health/**",
        "/actuator/info",
        "/api/catalog/products",
        "/api/catalog/products/**",
        "/api/search/**",
        "/api/media/public/**"
    };

    public static final String[] ADMIN_PATHS = {
        "/api/reports/**",
        "/api/catalog/admin/**",
        "/api/pricing/admin/**",
        "/api/accounts/admin/**"
    };

    public static final List<String> ADMIN_PATH_LIST = List.of(ADMIN_PATHS);

    private GatewayPaths() {
    }
}
