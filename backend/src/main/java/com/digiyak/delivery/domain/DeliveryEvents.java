package com.digiyak.delivery.domain;

import com.digiyak.delivery.domain.DeliveryEnums.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Spring Modulith domain events persisted via the Transactional Outbox ({@code modulith.event_publication})
 * so Dispatch and Finance react reliably with zero dual-write issues.
 */
public final class DeliveryEvents {

    private DeliveryEvents() {
    }

    public record DeliveryReadyForDispatchEvent(
            UUID deliveryRequestId,
            String trackingCode,
            UUID campusId,
            PaymentMethod paymentMethod,
            String pickupLabel,
            BigDecimal pickupLat,
            BigDecimal pickupLng,
            String dropoffLabel,
            BigDecimal dropoffLat,
            BigDecimal dropoffLng,
            long runnerPayoutPesewas,
            Instant occurredAt
    ) {
    }

    public record DeliveryHandoffPaymentRequestedEvent(
            UUID deliveryRequestId,
            String trackingCode,
            String shortPayLinkCode,
            long amountPesewas,
            String recipientPhone,
            Instant occurredAt
    ) {
    }

    public record DeliveryCompletedEvent(
            UUID deliveryRequestId,
            String trackingCode,
            UUID campusId,
            UUID runnerUserId,
            PaymentMethod paymentMethod,
            long deliveryFeePesewas,
            long runnerPayoutPesewas,
            long platformCutPesewas,
            boolean cashCollected,
            Instant occurredAt
    ) {
    }

    public record DeliveryCancelledEvent(
            UUID deliveryRequestId,
            String trackingCode,
            UUID campusId,
            UUID requesterUserId,
            PaymentMethod paymentMethod,
            boolean requiresRefund,
            String reason,
            Instant occurredAt
    ) {
    }
}
