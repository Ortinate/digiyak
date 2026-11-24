package com.digiyak.delivery.api.dto;

import com.digiyak.delivery.domain.DeliveryEnums.CashCollectionStatus;
import com.digiyak.delivery.domain.DeliveryEnums.DeliverySource;
import com.digiyak.delivery.domain.DeliveryEnums.DeliveryStatus;
import com.digiyak.delivery.domain.DeliveryEnums.PackageCategory;
import com.digiyak.delivery.domain.DeliveryEnums.PayLinkStatus;
import com.digiyak.delivery.domain.DeliveryEnums.PaymentMethod;
import com.digiyak.delivery.domain.DeliveryEnums.PaymentTiming;
import com.digiyak.delivery.domain.DeliveryRequest;
import com.digiyak.delivery.domain.DeliveryStatusHistory;
import com.digiyak.delivery.domain.PaymentLink;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class DeliveryDtos {

    private DeliveryDtos() {
    }

    public record CreateDeliveryRequest(
            @NotNull UUID campusId,
            @NotNull DeliverySource source,
            @NotNull PackageCategory packageCategory,
            @NotBlank @Size(max = 255) String packageDescription,

            UUID pickupLandmarkId,
            @NotNull UUID pickupZoneId,
            @NotBlank @Size(max = 200) String pickupLabel,
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal pickupLat,
            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal pickupLng,
            @NotBlank @Size(max = 120) String pickupContactName,
            @NotBlank @Size(max = 20) String pickupContactPhone,
            @Size(max = 255) String pickupNotes,

            UUID dropoffLandmarkId,
            @NotNull UUID dropoffZoneId,
            @NotBlank @Size(max = 200) String dropoffLabel,
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal dropoffLat,
            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal dropoffLng,
            @NotBlank @Size(max = 120) String recipientName,
            @NotBlank @Size(max = 20) String recipientPhone,
            @Size(max = 255) String roomNotes,

            @NotNull PaymentMethod paymentMethod,
            PaymentTiming paymentTiming,
            @Positive long deliveryFeePesewas
    ) {
    }

    public record CancelDeliveryRequest(
            @Size(max = 255) String reason
    ) {
    }

    public record DeliveryResponse(
            UUID id,
            String trackingCode,
            UUID campusId,
            UUID requesterUserId,
            DeliverySource source,
            PackageCategory packageCategory,
            String packageDescription,
            String pickupLabel,
            BigDecimal pickupLat,
            BigDecimal pickupLng,
            String dropoffLabel,
            BigDecimal dropoffLat,
            BigDecimal dropoffLng,
            String recipientName,
            String roomNotes,
            PaymentMethod paymentMethod,
            PaymentTiming paymentTiming,
            CashCollectionStatus cashCollectionStatus,
            long deliveryFeePesewas,
            long runnerPayoutPesewas,
            long platformCutPesewas,
            String pickupCode,
            String deliveryPin,
            DeliveryStatus status,
            PaymentLinkResponse paymentLink,
            Instant createdAt
    ) {
        public static DeliveryResponse from(DeliveryRequest r, PaymentLinkResponse link) {
            return new DeliveryResponse(
                    r.getId(),
                    r.getTrackingCode(),
                    r.getCampusId(),
                    r.getRequesterUserId(),
                    r.getSource(),
                    r.getPackageCategory(),
                    r.getPackageDescription(),
                    r.getPickupLabel(),
                    r.getPickupLat(),
                    r.getPickupLng(),
                    r.getDropoffLabel(),
                    r.getDropoffLat(),
                    r.getDropoffLng(),
                    r.getRecipientName(),
                    r.getRoomNotes(),
                    r.getPaymentMethod(),
                    r.getPaymentTiming(),
                    r.getCashCollectionStatus(),
                    r.getDeliveryFeePesewas(),
                    r.getRunnerPayoutPesewas(),
                    r.getPlatformCutPesewas(),
                    r.getPickupCode(),
                    r.getDeliveryPin(),
                    r.getStatus(),
                    link,
                    r.getCreatedAt()
            );
        }
    }

    /** Public live-tracking projection (intentionally omits private pickupCode). */
    public record PublicTrackingResponse(
            String trackingCode,
            DeliveryStatus status,
            PackageCategory packageCategory,
            String pickupLabel,
            BigDecimal pickupLat,
            BigDecimal pickupLng,
            String dropoffLabel,
            BigDecimal dropoffLat,
            BigDecimal dropoffLng,
            PaymentMethod paymentMethod,
            long deliveryFeePesewas,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static PublicTrackingResponse from(DeliveryRequest r) {
            return new PublicTrackingResponse(
                    r.getTrackingCode(),
                    r.getStatus(),
                    r.getPackageCategory(),
                    r.getPickupLabel(),
                    r.getPickupLat(),
                    r.getPickupLng(),
                    r.getDropoffLabel(),
                    r.getDropoffLat(),
                    r.getDropoffLng(),
                    r.getPaymentMethod(),
                    r.getDeliveryFeePesewas(),
                    r.getCreatedAt(),
                    r.getUpdatedAt()
            );
        }
    }

    public record PaymentLinkResponse(
            String shortCode,
            String sharePath,
            String trackingCode,
            String pickupLabel,
            String dropoffLabel,
            String recipientName,
            long amountPesewas,
            PayLinkStatus status,
            Instant expiresAt,
            Instant paidAt
    ) {
        public static PaymentLinkResponse from(PaymentLink link, DeliveryRequest req) {
            return new PaymentLinkResponse(
                    link.getShortCode(),
                    "/pay/" + link.getShortCode(),
                    req.getTrackingCode(),
                    req.getPickupLabel(),
                    req.getDropoffLabel(),
                    req.getRecipientName(),
                    link.getAmountPesewas(),
                    link.getStatus(),
                    link.getExpiresAt(),
                    link.getPaidAt()
            );
        }
    }

    public record DeliveryHistoryResponse(
            UUID id,
            DeliveryStatus fromStatus,
            DeliveryStatus toStatus,
            UUID changedByUserId,
            String reason,
            Instant createdAt
    ) {
        public static DeliveryHistoryResponse from(DeliveryStatusHistory h) {
            return new DeliveryHistoryResponse(
                    h.getId(),
                    h.getFromStatus(),
                    h.getToStatus(),
                    h.getChangedByUserId(),
                    h.getReason(),
                    h.getCreatedAt()
            );
        }
    }
}
