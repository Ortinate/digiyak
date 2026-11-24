package com.digiyak.identity;

import java.util.UUID;

/**
 * Public Spring Modulith facade exposed by the Identity module to other domain modules.
 * Prevents cross-module access to {@code identity.internal} repositories or entities.
 */
public interface IdentityModuleApi {

    UserDeliveryEligibility getDeliveryEligibility(UUID userId);

    void recordNoShow(UUID userId);

    void recordAdminAction(
            UUID actorUserId,
            String actorRole,
            UUID targetCampusId,
            String action,
            String targetEntityType,
            UUID targetEntityId,
            String metadataJson,
            String ipAddress
    );

    record UserDeliveryEligibility(
            UUID userId,
            UUID campusId,
            String fullName,
            String phone,
            boolean active,
            boolean verified,
            boolean allowPostpaid,
            int noShowCount
    ) {
    }
}
