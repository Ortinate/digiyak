package com.digiyak.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "admin_audit_logs", schema = "identity")
public class AdminAuditLog {

    @Id
    private UUID id;

    @Column(name = "actor_user_id", nullable = false)
    private UUID actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_role", nullable = false, length = 32)
    private UserRole actorRole;

    @Column(name = "target_campus_id")
    private UUID targetCampusId;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "target_entity_type", nullable = false, length = 80)
    private String targetEntityType;

    @Column(name = "target_entity_id")
    private UUID targetEntityId;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AdminAuditLog() {
    }

    public AdminAuditLog(
            UUID actorUserId,
            UserRole actorRole,
            UUID targetCampusId,
            String action,
            String targetEntityType,
            UUID targetEntityId,
            String metadataJson,
            String ipAddress
    ) {
        this.id = UUID.randomUUID();
        this.actorUserId = actorUserId;
        this.actorRole = actorRole;
        this.targetCampusId = targetCampusId;
        this.action = action;
        this.targetEntityType = targetEntityType;
        this.targetEntityId = targetEntityId;
        this.metadataJson = metadataJson;
        this.ipAddress = ipAddress;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public UserRole getActorRole() {
        return actorRole;
    }

    public UUID getTargetCampusId() {
        return targetCampusId;
    }

    public String getAction() {
        return action;
    }

    public String getTargetEntityType() {
        return targetEntityType;
    }

    public UUID getTargetEntityId() {
        return targetEntityId;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
