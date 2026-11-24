package com.digiyak.delivery;

import com.digiyak.delivery.domain.DeliveryEnums.DeliveryStatus;
import com.digiyak.delivery.domain.DeliveryEnums.PaymentMethod;

import java.util.UUID;

/**
 * Public Spring Modulith facade exposed by the Delivery module to Dispatch and Finance modules.
 */
public interface DeliveryModuleApi {

    DeliverySummary getSummary(UUID deliveryRequestId);

    void markPaymentConfirmed(UUID deliveryRequestId, UUID actorUserId, String paymentReference);

    void markRunnerAssigned(UUID deliveryRequestId, UUID runnerUserId);

    void markPickedUp(UUID deliveryRequestId, UUID runnerUserId, String pickupCode, String pickupPhotoUrl);

    HandoffPaymentInfo requestHandoffPayment(UUID deliveryRequestId, UUID runnerUserId);

    void completeDelivery(
            UUID deliveryRequestId,
            UUID runnerUserId,
            String deliveryPin,
            boolean cashCollected,
            String dropoffPhotoUrl
    );

    void reportBuyerNoShow(UUID deliveryRequestId, UUID runnerUserId, String reason);

    record DeliverySummary(
            UUID id,
            String trackingCode,
            UUID campusId,
            UUID requesterUserId,
            PaymentMethod paymentMethod,
            DeliveryStatus status,
            long deliveryFeePesewas,
            long runnerPayoutPesewas,
            long platformCutPesewas
    ) {
    }

    record HandoffPaymentInfo(
            UUID deliveryRequestId,
            String shortCode,
            long amountPesewas
    ) {
    }
}
