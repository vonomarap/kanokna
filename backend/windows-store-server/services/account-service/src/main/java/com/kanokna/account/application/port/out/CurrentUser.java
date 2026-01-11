package com.kanokna.account.application.port.out;

import java.util.Set;

/**
 * Current user context from the identity provider.
 */
public record CurrentUser(
    String userId,
    String email,
    String firstName,
    String lastName,
    String phoneNumber,
    Set<String> roles
) {
    public boolean isAdmin() {
        return roles != null && (roles.contains("ADMIN") || roles.contains("ROLE_ADMIN"));
    }
}