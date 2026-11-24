package com.digiyak.shared;

import java.util.Set;
import java.util.UUID;

/**
 * Immutable security principal exposed to all domain controllers after JWT authentication.
 * Note: {@code campusId} may be {@code null} for global {@code SUPER_ADMIN} accounts.
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

    public boolean isSuperAdmin() {
        return hasRole("SUPER_ADMIN");
    }

    public boolean isCampusAdmin() {
        return hasRole("CAMPUS_ADMIN");
    }

    public boolean isAnyAdmin() {
        return isSuperAdmin() || isCampusAdmin();
    }

    public boolean isRunner() {
        return hasRole("RUNNER");
    }

    /**
     * Enforces multi-campus tenant isolation: SUPER_ADMIN can manage any campus,
     * whereas CAMPUS_ADMIN can only manage their assigned campus.
     */
    public boolean canManageCampus(UUID targetCampusId) {
        if (isSuperAdmin()) {
            return true;
        }
        return isCampusAdmin() && campusId != null && campusId.equals(targetCampusId);
    }
}
