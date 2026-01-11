package com.kanokna.account.adapters.out.keycloak;

import com.kanokna.account.application.port.out.CurrentUser;
import com.kanokna.account.application.port.out.CurrentUserProvider;
import com.kanokna.account.domain.exception.AccountDomainErrors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Current user provider backed by JWT claims from Keycloak.
 */
@Component
public class KeycloakCurrentUserProvider implements CurrentUserProvider {
    @Override
    public CurrentUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw AccountDomainErrors.unauthorized("Authentication required");
        }

        String userId = null;
        String email = null;
        String firstName = null;
        String lastName = null;
        String phoneNumber = null;

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            userId = jwt.getSubject();
            email = jwt.getClaimAsString("email");
            firstName = jwt.getClaimAsString("given_name");
            lastName = jwt.getClaimAsString("family_name");
            phoneNumber = jwt.getClaimAsString("phone_number");
        } else {
            userId = authentication.getName();
        }

        Set<String> roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

        return new CurrentUser(userId, email, firstName, lastName, phoneNumber, roles);
    }
}
