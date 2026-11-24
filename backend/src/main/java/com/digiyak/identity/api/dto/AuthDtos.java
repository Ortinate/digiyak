package com.digiyak.identity.api.dto;

import com.digiyak.identity.domain.AdminAuditLog;
import com.digiyak.identity.domain.MomoProvider;
import com.digiyak.identity.domain.User;
import com.digiyak.identity.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotNull UUID campusId,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 9, max = 20) String phone,
            @NotBlank @Size(min = 8, max = 100) String password,
            @NotBlank @Size(min = 2, max = 150) String fullName,
            @Size(max = 64) String studentIdNumber,
            MomoProvider momoProvider,
            @Size(max = 20) String momoNumber
    ) {
    }

    public record LoginRequest(
            @NotBlank String identifier,
            @NotBlank String password
    ) {
    }

    public record RefreshRequest(
            @NotBlank String refreshToken
    ) {
    }

    public record UpdateMomoRequest(
            @NotNull MomoProvider momoProvider,
            @NotBlank @Size(min = 9, max = 20) String momoNumber
    ) {
    }

    public record OptInVendorRequest(
            @NotBlank @Size(min = 2, max = 150) String businessName
    ) {
    }

    public record AppointCampusAdminRequest(
            @NotNull UUID userId,
            @NotNull UUID campusId
    ) {
    }

    public record OverridePostpaidRequest(
            @NotNull Boolean allowPostpaid,
            @Size(max = 255) String reason
    ) {
    }

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            long expiresInSeconds,
            UserProfileResponse user
    ) {
    }

    public record UserProfileResponse(
            UUID id,
            UUID campusId,
            String email,
            String phone,
            String fullName,
            String studentIdNumber,
            String businessName,
            MomoProvider momoProvider,
            String momoNumber,
            int noShowCount,
            boolean allowPostpaid,
            boolean verified,
            boolean active,
            Set<UserRole> roles,
            Instant createdAt
    ) {
        public static UserProfileResponse from(User user) {
            return new UserProfileResponse(
                    user.getId(),
                    user.getCampusId(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getFullName(),
                    user.getStudentIdNumber(),
                    user.getBusinessName(),
                    user.getMomoProvider(),
                    user.getMomoNumber(),
                    user.getNoShowCount(),
                    user.isAllowPostpaid(),
                    user.isVerified(),
                    user.isActive(),
                    user.getRoles(),
                    user.getCreatedAt()
            );
        }
    }

    public record AuditLogResponse(
            UUID id,
            UUID actorUserId,
            UserRole actorRole,
            UUID targetCampusId,
            String action,
            String targetEntityType,
            UUID targetEntityId,
            String metadataJson,
            String ipAddress,
            Instant createdAt
    ) {
        public static AuditLogResponse from(AdminAuditLog log) {
            return new AuditLogResponse(
                    log.getId(),
                    log.getActorUserId(),
                    log.getActorRole(),
                    log.getTargetCampusId(),
                    log.getAction(),
                    log.getTargetEntityType(),
                    log.getTargetEntityId(),
                    log.getMetadataJson(),
                    log.getIpAddress(),
                    log.getCreatedAt()
            );
        }
    }
}
