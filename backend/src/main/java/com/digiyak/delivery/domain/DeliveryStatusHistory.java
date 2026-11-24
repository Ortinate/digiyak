package com.digiyak.delivery.domain;

import com.digiyak.delivery.domain.DeliveryEnums.DeliveryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "delivery_status_history", schema = "delivery")
public class DeliveryStatusHistory {

    @Id
    private UUID id;

    @Column(name = "delivery_request_id", nullable = false)
    private UUID deliveryRequestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 32)
    private DeliveryStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 32)
    private DeliveryStatus toStatus;

    @Column(name = "changed_by_user_id")
    private UUID changedByUserId;

    @Column(length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected DeliveryStatusHistory() {
    }

    public DeliveryStatusHistory(
            UUID deliveryRequestId,
            DeliveryStatus fromStatus,
            DeliveryStatus toStatus,
            UUID changedByUserId,
            String reason
    ) {
        this.id = UUID.randomUUID();
        this.deliveryRequestId = deliveryRequestId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedByUserId = changedByUserId;
        this.reason = reason;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getDeliveryRequestId() { return deliveryRequestId; }
    public DeliveryStatus getFromStatus() { return fromStatus; }
    public DeliveryStatus getToStatus() { return toStatus; }
    public UUID getChangedByUserId() { return changedByUserId; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
}
