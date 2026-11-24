package com.digiyak.shared;

import java.util.Set;
import java.util.UUID;

/**
 * Immutable security principal exposed to all domain controllers after JWT authentication.
 */
public record AuthenticatedUser(
        UUID userId,
        UUID campusId,
        String email,
        Set<String> roles
) {

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isRunner() {
        return hasRole("RUNNER");
    }
}
