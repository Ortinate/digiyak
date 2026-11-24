package com.digiyak.delivery.domain;

import com.digiyak.delivery.domain.DeliveryEnums.CashCollectionStatus;
import com.digiyak.delivery.domain.DeliveryEnums.DeliverySource;
import com.digiyak.delivery.domain.DeliveryEnums.DeliveryStatus;
import com.digiyak.delivery.domain.DeliveryEnums.PackageCategory;
import com.digiyak.delivery.domain.DeliveryEnums.PaymentMethod;
import com.digiyak.delivery.domain.DeliveryEnums.PaymentTiming;
import com.digiyak.shared.ApiException;
import com.digiyak.shared.CodeGenerator;
import com.digiyak.shared.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "delivery_requests", schema = "delivery")
public class DeliveryRequest {

    @Id
    private UUID id;

    @Column(name = "tracking_code", nullable = false, unique = true, length = 32)
    private String trackingCode;

    @Column(name = "campus_id", nullable = false)
    private UUID campusId;

    @Column(name = "requester_user_id", nullable = false)
    private UUID requesterUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DeliverySource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "package_category", nullable = false, length = 32)
    private PackageCategory packageCategory;

    @Column(name = "package_description", nullable = false, length = 255)
    private String packageDescription;

    @Column(name = "pickup_landmark_id")
    private UUID pickupLandmarkId;

    @Column(name = "pickup_zone_id", nullable = false)
    private UUID pickupZoneId;

    @Column(name = "pickup_label", nullable = false, length = 200)
    private String pickupLabel;

    @Column(name = "pickup_lat", nullable = false, precision = 9, scale = 6)
    private BigDecimal pickupLat;

    @Column(name = "pickup_lng", nullable = false, precision = 9, scale = 6)
    private BigDecimal pickupLng;

    @Column(name = "pickup_contact_name", nullable = false, length = 120)
    private String pickupContactName;

    @Column(name = "pickup_contact_phone", nullable = false, length = 20)
    private String pickupContactPhone;

    @Column(name = "pickup_notes", length = 255)
    private String pickupNotes;

    @Column(name = "dropoff_landmark_id")
    private UUID dropoffLandmarkId;

    @Column(name = "dropoff_zone_id", nullable = false)
    private UUID dropoffZoneId;

    @Column(name = "dropoff_label", nullable = false, length = 200)
    private String dropoffLabel;

    @Column(name = "dropoff_lat", nullable = false, precision = 9, scale = 6)
    private BigDecimal dropoffLat;

    @Column(name = "dropoff_lng", nullable = false, precision = 9, scale = 6)
    private BigDecimal dropoffLng;

    @Column(name = "recipient_name", nullable = false, length = 120)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @Column(name = "room_notes", length = 255)
    private String roomNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 32)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_timing", nullable = false, length = 32)
    private PaymentTiming paymentTiming;

    @Enumerated(EnumType.STRING)
    @Column(name = "cash_collection_status", nullable = false, length = 32)
    private CashCollectionStatus cashCollectionStatus;

    @Column(name = "cash_confirmed_at")
    private Instant cashConfirmedAt;

    @Column(name = "delivery_fee_pesewas", nullable = false)
    private long deliveryFeePesewas;

    @Column(name = "runner_payout_pesewas", nullable = false)
    private long runnerPayoutPesewas;

    @Column(name = "platform_cut_pesewas", nullable = false)
    private long platformCutPesewas;

    @Column(name = "pickup_code", nullable = false, length = 6)
    private String pickupCode;

    @Column(name = "delivery_pin", nullable = false, length = 6)
    private String deliveryPin;

    @Column(name = "pickup_photo_url", length = 500)
    private String pickupPhotoUrl;

    @Column(name = "dropoff_photo_url", length = 500)
    private String dropoffPhotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DeliveryStatus status;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 128)
    private String idempotencyKey;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected DeliveryRequest() {
    }

    public static DeliveryRequest create(
            UUID campusId,
            UUID requesterUserId,
            DeliverySource source,
            PackageCategory packageCategory,
            String packageDescription,
            UUID pickupLandmarkId,
            UUID pickupZoneId,
            String pickupLabel,
            BigDecimal pickupLat,
            BigDecimal pickupLng,
            String pickupContactName,
            String pickupContactPhone,
            String pickupNotes,
            UUID dropoffLandmarkId,
            UUID dropoffZoneId,
            String dropoffLabel,
            BigDecimal dropoffLat,
            BigDecimal dropoffLng,
            String recipientName,
            String recipientPhone,
            String roomNotes,
            PaymentMethod paymentMethod,
            PaymentTiming paymentTiming,
            long deliveryFeePesewas,
            int platformCutBps,
            String idempotencyKey
    ) {
        if (deliveryFeePesewas <= 0) {
            throw ApiException.badRequest("INVALID_DELIVERY_FEE", "Delivery fee must be positive");
        }

        Money.TwoWaySplit split = Money.ghs(deliveryFeePesewas).splitTwoWays(platformCutBps);

        DeliveryRequest req = new DeliveryRequest();
        req.id = UUID.randomUUID();
        req.trackingCode = CodeGenerator.trackingCode();
        req.campusId = campusId;
        req.requesterUserId = requesterUserId;
        req.source = source;
        req.packageCategory = packageCategory;
        req.packageDescription = packageDescription.trim();

        req.pickupLandmarkId = pickupLandmarkId;
        req.pickupZoneId = pickupZoneId;
        req.pickupLabel = pickupLabel.trim();
        req.pickupLat = pickupLat;
        req.pickupLng = pickupLng;
        req.pickupContactName = pickupContactName.trim();
        req.pickupContactPhone = pickupContactPhone.trim();
        req.pickupNotes = pickupNotes != null ? pickupNotes.trim() : null;

        req.dropoffLandmarkId = dropoffLandmarkId;
        req.dropoffZoneId = dropoffZoneId;
        req.dropoffLabel = dropoffLabel.trim();
        req.dropoffLat = dropoffLat;
        req.dropoffLng = dropoffLng;
        req.recipientName = recipientName.trim();
        req.recipientPhone = recipientPhone.trim();
        req.roomNotes = roomNotes != null ? roomNotes.trim() : null;

        req.paymentMethod = paymentMethod;
        req.paymentTiming = paymentTiming != null ? paymentTiming : defaultTimingFor(paymentMethod);
        req.cashCollectionStatus = paymentMethod == PaymentMethod.PHYSICAL_CASH
                ? CashCollectionStatus.PENDING_COLLECTION
                : CashCollectionStatus.NOT_APPLICABLE;

        req.deliveryFeePesewas = deliveryFeePesewas;
        req.runnerPayoutPesewas = split.runnerShare().pesewas();
        req.platformCutPesewas = split.platformCut().pesewas();

        req.pickupCode = CodeGenerator.fourDigitPin();
        req.deliveryPin = CodeGenerator.fourDigitPin();

        // Postpaid modes (Pay-After-Delivery MoMo & Physical Cash) dispatch immediately; prepaid waits for MoMo webhook.
        req.status = paymentMethod.isPostpaid()
                ? DeliveryStatus.FINDING_RUNNER
                : DeliveryStatus.AWAITING_PAYMENT;

        req.idempotencyKey = idempotencyKey;
        req.createdAt = Instant.now();
        req.updatedAt = req.createdAt;
        return req;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    private static PaymentTiming defaultTimingFor(PaymentMethod method) {
        return switch (method) {
            case MOMO_PREPAID, VENDOR_WALLET -> PaymentTiming.PREPAID;
            case MOMO_PAY_AFTER_DELIVERY, PHYSICAL_CASH -> PaymentTiming.PAY_ON_DELIVERY;
        };
    }

    public DeliveryStatus markPaidAndDispatch() {
        if (this.status == DeliveryStatus.AWAITING_PAYMENT) {
            DeliveryStatus previous = this.status;
            this.status = DeliveryStatus.FINDING_RUNNER;
            return previous;
        }
        if (this.status == DeliveryStatus.AWAITING_HANDOFF_PAYMENT) {
            DeliveryStatus previous = this.status;
            this.status = DeliveryStatus.DELIVERED;
            return previous;
        }
        throw ApiException.conflict("INVALID_PAYMENT_STATE",
                "Cannot mark payment confirmed from status " + this.status);
    }

    public DeliveryStatus markRunnerAssigned() {
        requireStatus(DeliveryStatus.FINDING_RUNNER, "Delivery is not open for runner assignment");
        DeliveryStatus previous = this.status;
        this.status = DeliveryStatus.RUNNER_ASSIGNED;
        return previous;
    }

    public DeliveryStatus markPickedUp(String providedPickupCode, String photoUrl) {
        if (this.status != DeliveryStatus.RUNNER_ASSIGNED && this.status != DeliveryStatus.AT_PICKUP) {
            throw ApiException.conflict("INVALID_PICKUP_STATE", "Delivery cannot be picked up in status " + this.status);
        }
        if (!this.pickupCode.equals(providedPickupCode)) {
            throw ApiException.unprocessable("INVALID_PICKUP_CODE", "Pickup code does not match");
        }
        DeliveryStatus previous = this.status;
        this.pickupPhotoUrl = photoUrl;
        this.status = DeliveryStatus.IN_TRANSIT;
        return previous;
    }

    public DeliveryStatus markAwaitingHandoffPayment() {
        if (this.paymentMethod != PaymentMethod.MOMO_PAY_AFTER_DELIVERY) {
            throw ApiException.badRequest("NOT_PAY_AFTER_DELIVERY",
                    "Handoff MoMo payment applies only to MOMO_PAY_AFTER_DELIVERY deliveries");
        }
        requireStatus(DeliveryStatus.IN_TRANSIT, "Delivery must be IN_TRANSIT to request handoff payment");
        DeliveryStatus previous = this.status;
        this.status = DeliveryStatus.AWAITING_HANDOFF_PAYMENT;
        return previous;
    }

    public DeliveryStatus completeWithPin(String providedPin, boolean cashCollected, String photoUrl) {
        if (this.status != DeliveryStatus.IN_TRANSIT && this.status != DeliveryStatus.AWAITING_HANDOFF_PAYMENT) {
            throw ApiException.conflict("INVALID_COMPLETION_STATE",
                    "Delivery cannot be completed from status " + this.status);
        }
        if (!this.deliveryPin.equals(providedPin)) {
            throw ApiException.unprocessable("INVALID_DELIVERY_PIN", "4-digit Delivery PIN is incorrect");
        }
        if (this.paymentMethod == PaymentMethod.MOMO_PAY_AFTER_DELIVERY
                && this.status != DeliveryStatus.DELIVERED) {
            throw ApiException.conflict("HANDOFF_MOMO_UNPAID",
                    "Recipient must complete the MoMo Pay-After-Delivery prompt before final handoff");
        }
        if (this.paymentMethod == PaymentMethod.PHYSICAL_CASH) {
            if (!cashCollected) {
                throw ApiException.badRequest("CASH_CONFIRMATION_REQUIRED",
                        "Runner must confirm physical cash collection before completing a cash delivery");
            }
            this.cashCollectionStatus = CashCollectionStatus.COLLECTED_BY_RUNNER;
            this.cashConfirmedAt = Instant.now();
        }
        DeliveryStatus previous = this.status;
        this.dropoffPhotoUrl = photoUrl;
        this.status = DeliveryStatus.DELIVERED;
        return previous;
    }

    public DeliveryStatus cancel() {
        if (this.status == DeliveryStatus.DELIVERED || this.status == DeliveryStatus.CANCELLED
                || this.status == DeliveryStatus.REFUNDED) {
            throw ApiException.conflict("CANNOT_CANCEL", "Delivery cannot be cancelled in status " + this.status);
        }
        if (this.status == DeliveryStatus.IN_TRANSIT) {
            throw ApiException.conflict("IN_TRANSIT_CANCEL_BLOCKED",
                    "Cannot self-cancel while runner is already in transit; contact campus support");
        }
        DeliveryStatus previous = this.status;
        this.status = DeliveryStatus.CANCELLED;
        return previous;
    }

    public boolean requiresRefundOnCancel(DeliveryStatus statusBeforeCancel) {
        return !this.paymentMethod.isPostpaid()
                && statusBeforeCancel != DeliveryStatus.AWAITING_PAYMENT;
    }

    private void requireStatus(DeliveryStatus expected, String message) {
        if (this.status != expected) {
            throw ApiException.conflict("INVALID_STATUS_TRANSITION", message + " (current: " + this.status + ")");
        }
    }

    public UUID getId() { return id; }
    public String getTrackingCode() { return trackingCode; }
    public UUID getCampusId() { return campusId; }
    public UUID getRequesterUserId() { return requesterUserId; }
    public DeliverySource getSource() { return source; }
    public PackageCategory getPackageCategory() { return packageCategory; }
    public String getPackageDescription() { return packageDescription; }
    public UUID getPickupLandmarkId() { return pickupLandmarkId; }
    public UUID getPickupZoneId() { return pickupZoneId; }
    public String getPickupLabel() { return pickupLabel; }
    public BigDecimal getPickupLat() { return pickupLat; }
    public BigDecimal getPickupLng() { return pickupLng; }
    public String getPickupContactName() { return pickupContactName; }
    public String getPickupContactPhone() { return pickupContactPhone; }
    public String getPickupNotes() { return pickupNotes; }
    public UUID getDropoffLandmarkId() { return dropoffLandmarkId; }
    public UUID getDropoffZoneId() { return dropoffZoneId; }
    public String getDropoffLabel() { return dropoffLabel; }
    public BigDecimal getDropoffLat() { return dropoffLat; }
    public BigDecimal getDropoffLng() { return dropoffLng; }
    public String getRecipientName() { return recipientName; }
    public String getRecipientPhone() { return recipientPhone; }
    public String getRoomNotes() { return roomNotes; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public PaymentTiming getPaymentTiming() { return paymentTiming; }
    public CashCollectionStatus getCashCollectionStatus() { return cashCollectionStatus; }
    public Instant getCashConfirmedAt() { return cashConfirmedAt; }
    public long getDeliveryFeePesewas() { return deliveryFeePesewas; }
    public long getRunnerPayoutPesewas() { return runnerPayoutPesewas; }
    public long getPlatformCutPesewas() { return platformCutPesewas; }
    public String getPickupCode() { return pickupCode; }
    public String getDeliveryPin() { return deliveryPin; }
    public String getPickupPhotoUrl() { return pickupPhotoUrl; }
    public String getDropoffPhotoUrl() { return dropoffPhotoUrl; }
    public DeliveryStatus getStatus() { return status; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
