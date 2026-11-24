package com.digiyak.delivery.internal;

import com.digiyak.delivery.DeliveryModuleApi;
import com.digiyak.delivery.api.dto.DeliveryDtos.CreateDeliveryRequest;
import com.digiyak.delivery.api.dto.DeliveryDtos.DeliveryHistoryResponse;
import com.digiyak.delivery.api.dto.DeliveryDtos.DeliveryResponse;
import com.digiyak.delivery.api.dto.DeliveryDtos.PaymentLinkResponse;
import com.digiyak.delivery.api.dto.DeliveryDtos.PublicTrackingResponse;
import com.digiyak.delivery.domain.DeliveryEnums.DeliverySource;
import com.digiyak.delivery.domain.DeliveryEnums.DeliveryStatus;
import com.digiyak.delivery.domain.DeliveryEvents.DeliveryCancelledEvent;
import com.digiyak.delivery.domain.DeliveryEvents.DeliveryCompletedEvent;
import com.digiyak.delivery.domain.DeliveryEvents.DeliveryHandoffPaymentRequestedEvent;
import com.digiyak.delivery.domain.DeliveryEvents.DeliveryReadyForDispatchEvent;
import com.digiyak.delivery.domain.DeliveryRequest;
import com.digiyak.delivery.domain.DeliveryStatusHistory;
import com.digiyak.delivery.domain.PaymentLink;
import com.digiyak.identity.IdentityModuleApi;
import com.digiyak.identity.IdentityModuleApi.UserDeliveryEligibility;
import com.digiyak.shared.ApiException;
import com.digiyak.shared.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class DeliveryService implements DeliveryModuleApi {

    private final DeliveryRequestRepository deliveryRequestRepository;
    private final DeliveryStatusHistoryRepository historyRepository;
    private final PaymentLinkService paymentLinkService;
    private final IdentityModuleApi identityModuleApi;
    private final ApplicationEventPublisher eventPublisher;
    private final int defaultPlatformCutBps;

    public DeliveryService(
            DeliveryRequestRepository deliveryRequestRepository,
            DeliveryStatusHistoryRepository historyRepository,
            PaymentLinkService paymentLinkService,
            IdentityModuleApi identityModuleApi,
            ApplicationEventPublisher eventPublisher,
            @Value("${digiyak.economics.default-platform-cut-bps:1500}") int defaultPlatformCutBps
    ) {
        this.deliveryRequestRepository = deliveryRequestRepository;
        this.historyRepository = historyRepository;
        this.paymentLinkService = paymentLinkService;
        this.identityModuleApi = identityModuleApi;
        this.eventPublisher = eventPublisher;
        this.defaultPlatformCutBps = defaultPlatformCutBps;
    }

    public DeliveryResponse createDelivery(
            AuthenticatedUser actor,
            CreateDeliveryRequest req,
            String idempotencyKey
    ) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw ApiException.badRequest("IDEMPOTENCY_KEY_REQUIRED", "Idempotency-Key header is required");
        }

        // Idempotent replay: return existing request cleanly on flaky campus network retries
        Optional<DeliveryRequest> existing = deliveryRequestRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            DeliveryRequest replay = existing.get();
            if (!replay.getRequesterUserId().equals(actor.userId())) {
                throw ApiException.conflict("IDEMPOTENCY_KEY_CONFLICT", "Idempotency-Key already used by another user");
            }
            return toResponse(replay);
        }

        UserDeliveryEligibility eligibility = identityModuleApi.getDeliveryEligibility(actor.userId());
        if (!eligibility.active()) {
            throw ApiException.forbidden("ACCOUNT_SUSPENDED", "Suspended accounts cannot create deliveries");
        }
        if (req.paymentMethod().isPostpaid() && !eligibility.allowPostpaid()) {
            throw ApiException.forbidden("POSTPAID_RESTRICTED",
                    "Pay-After-Delivery and Physical Cash are restricted due to repeated no-shows; please use MoMo Prepaid");
        }

        DeliveryRequest created = DeliveryRequest.create(
                req.campusId(),
                actor.userId(),
                req.source(),
                req.packageCategory(),
                req.packageDescription(),
                req.pickupLandmarkId(),
                req.pickupZoneId(),
                req.pickupLabel(),
                req.pickupLat(),
                req.pickupLng(),
                req.pickupContactName(),
                req.pickupContactPhone(),
                req.pickupNotes(),
                req.dropoffLandmarkId(),
                req.dropoffZoneId(),
                req.dropoffLabel(),
                req.dropoffLat(),
                req.dropoffLng(),
                req.recipientName(),
                req.recipientPhone(),
                req.roomNotes(),
                req.paymentMethod(),
                req.paymentTiming(),
                req.deliveryFeePesewas(),
                defaultPlatformCutBps,
                idempotencyKey
        );

        deliveryRequestRepository.save(created);
        recordHistory(created.getId(), null, created.getStatus(), actor.userId(), "Delivery request created");

        PaymentLinkResponse linkResponse = null;
        if (req.source() == DeliverySource.VENDOR_PAY_LINK) {
            PaymentLink link = paymentLinkService.getOrCreateForDelivery(created);
            linkResponse = PaymentLinkResponse.from(link, created);
        }

        // Postpaid deliveries (Pay-After-Delivery MoMo & Physical Cash) broadcast to campus runners immediately
        if (created.getStatus() == DeliveryStatus.FINDING_RUNNER) {
            publishReadyForDispatch(created);
        }

        return DeliveryResponse.from(created, linkResponse);
    }

    @Transactional(readOnly = true)
    public Page<DeliveryResponse> listMyDeliveries(UUID userId, Pageable pageable) {
        return deliveryRequestRepository.findByRequesterUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryForActor(AuthenticatedUser actor, UUID deliveryId) {
        DeliveryRequest req = requireDelivery(deliveryId);
        boolean isOwner = req.getRequesterUserId().equals(actor.userId());
        if (!isOwner && !actor.canManageCampus(req.getCampusId())) {
            throw ApiException.forbidden("DELIVERY_ACCESS_DENIED", "You do not have access to this delivery");
        }
        return toResponse(req);
    }

    @Transactional(readOnly = true)
    public PublicTrackingResponse trackByCode(String trackingCode) {
        DeliveryRequest req = deliveryRequestRepository.findByTrackingCode(trackingCode.trim().toUpperCase())
                .orElseThrow(() -> ApiException.notFound("DELIVERY_NOT_FOUND", "Tracking code not found"));
        return PublicTrackingResponse.from(req);
    }

    @Transactional(readOnly = true)
    public List<DeliveryHistoryResponse> getHistory(AuthenticatedUser actor, UUID deliveryId) {
        DeliveryRequest req = requireDelivery(deliveryId);
        if (!req.getRequesterUserId().equals(actor.userId()) && !actor.canManageCampus(req.getCampusId())) {
            throw ApiException.forbidden("DELIVERY_ACCESS_DENIED", "You do not have access to this delivery");
        }
        return historyRepository.findByDeliveryRequestIdOrderByCreatedAtAsc(deliveryId).stream()
                .map(DeliveryHistoryResponse::from)
                .toList();
    }

    public PaymentLinkResponse createOrGetPaymentLink(AuthenticatedUser actor, UUID deliveryId) {
        DeliveryRequest req = requireDelivery(deliveryId);
        if (!req.getRequesterUserId().equals(actor.userId()) && !actor.canManageCampus(req.getCampusId())) {
            throw ApiException.forbidden("DELIVERY_ACCESS_DENIED", "Cannot create payment link for another user's delivery");
        }
        PaymentLink link = paymentLinkService.getOrCreateForDelivery(req);
        return PaymentLinkResponse.from(link, req);
    }

    public DeliveryResponse cancelDelivery(AuthenticatedUser actor, UUID deliveryId, String reason) {
        DeliveryRequest req = requireDelivery(deliveryId);
        if (!req.getRequesterUserId().equals(actor.userId()) && !actor.canManageCampus(req.getCampusId())) {
            throw ApiException.forbidden("DELIVERY_ACCESS_DENIED", "Cannot cancel another user's delivery");
        }
        DeliveryStatus previous = req.cancel();
        deliveryRequestRepository.save(req);
        paymentLinkService.cancelIfPresent(req.getId());
        recordHistory(req.getId(), previous, req.getStatus(), actor.userId(),
                reason != null ? reason : "Cancelled by requester");

        eventPublisher.publishEvent(new DeliveryCancelledEvent(
                req.getId(),
                req.getTrackingCode(),
                req.getCampusId(),
                req.getRequesterUserId(),
                req.getPaymentMethod(),
                req.requiresRefundOnCancel(previous),
                reason,
                Instant.now()
        ));

        return toResponse(req);
    }

    // ---- DeliveryModuleApi implementation (called by Dispatch & Finance) ----

    @Override
    @Transactional(readOnly = true)
    public DeliverySummary getSummary(UUID deliveryRequestId) {
        DeliveryRequest r = requireDelivery(deliveryRequestId);
        return new DeliverySummary(
                r.getId(),
                r.getTrackingCode(),
                r.getCampusId(),
                r.getRequesterUserId(),
                r.getPaymentMethod(),
                r.getStatus(),
                r.getDeliveryFeePesewas(),
                r.getRunnerPayoutPesewas(),
                r.getPlatformCutPesewas()
        );
    }

    @Override
    public void markPaymentConfirmed(UUID deliveryRequestId, UUID actorUserId, String paymentReference) {
        DeliveryRequest req = requireDelivery(deliveryRequestId);
        DeliveryStatus previous = req.markPaidAndDispatch();
        deliveryRequestRepository.save(req);
        paymentLinkService.markPaidIfPresent(deliveryRequestId);
        recordHistory(req.getId(), previous, req.getStatus(), actorUserId,
                "Payment confirmed (" + paymentReference + ")");

        if (req.getStatus() == DeliveryStatus.FINDING_RUNNER) {
            publishReadyForDispatch(req);
        } else if (req.getStatus() == DeliveryStatus.DELIVERED) {
            eventPublisher.publishEvent(new DeliveryCompletedEvent(
                    req.getId(),
                    req.getTrackingCode(),
                    req.getCampusId(),
                    actorUserId,
                    req.getPaymentMethod(),
                    req.getDeliveryFeePesewas(),
                    req.getRunnerPayoutPesewas(),
                    req.getPlatformCutPesewas(),
                    false,
                    Instant.now()
            ));
        }
    }

    @Override
    public void markRunnerAssigned(UUID deliveryRequestId, UUID runnerUserId) {
        DeliveryRequest req = requireDelivery(deliveryRequestId);
        DeliveryStatus previous = req.markRunnerAssigned();
        deliveryRequestRepository.save(req);
        recordHistory(req.getId(), previous, req.getStatus(), runnerUserId, "Runner claimed delivery");
    }

    @Override
    public void markPickedUp(UUID deliveryRequestId, UUID runnerUserId, String pickupCode, String pickupPhotoUrl) {
        DeliveryRequest req = requireDelivery(deliveryRequestId);
        DeliveryStatus previous = req.markPickedUp(pickupCode, pickupPhotoUrl);
        deliveryRequestRepository.save(req);
        recordHistory(req.getId(), previous, req.getStatus(), runnerUserId, "Pickup verified via 4-digit code");
    }

    @Override
    public HandoffPaymentInfo requestHandoffPayment(UUID deliveryRequestId, UUID runnerUserId) {
        DeliveryRequest req = requireDelivery(deliveryRequestId);
        DeliveryStatus previous = req.markAwaitingHandoffPayment();
        deliveryRequestRepository.save(req);
        PaymentLink link = paymentLinkService.getOrCreateForDelivery(req);
        recordHistory(req.getId(), previous, req.getStatus(), runnerUserId,
                "Pay-After-Delivery MoMo handoff prompt initiated");

        eventPublisher.publishEvent(new DeliveryHandoffPaymentRequestedEvent(
                req.getId(),
                req.getTrackingCode(),
                link.getShortCode(),
                req.getDeliveryFeePesewas(),
                req.getRecipientPhone(),
                Instant.now()
        ));

        return new HandoffPaymentInfo(req.getId(), link.getShortCode(), req.getDeliveryFeePesewas());
    }

    @Override
    public void completeDelivery(
            UUID deliveryRequestId,
            UUID runnerUserId,
            String deliveryPin,
            boolean cashCollected,
            String dropoffPhotoUrl
    ) {
        DeliveryRequest req = requireDelivery(deliveryRequestId);
        DeliveryStatus previous = req.completeWithPin(deliveryPin, cashCollected, dropoffPhotoUrl);
        deliveryRequestRepository.save(req);
        recordHistory(req.getId(), previous, req.getStatus(), runnerUserId,
                cashCollected ? "Delivered & physical cash confirmed via PIN" : "Delivered & verified via PIN");

        eventPublisher.publishEvent(new DeliveryCompletedEvent(
                req.getId(),
                req.getTrackingCode(),
                req.getCampusId(),
                runnerUserId,
                req.getPaymentMethod(),
                req.getDeliveryFeePesewas(),
                req.getRunnerPayoutPesewas(),
                req.getPlatformCutPesewas(),
                cashCollected,
                Instant.now()
        ));
    }

    @Override
    public void reportBuyerNoShow(UUID deliveryRequestId, UUID runnerUserId, String reason) {
        DeliveryRequest req = requireDelivery(deliveryRequestId);
        identityModuleApi.recordNoShow(req.getRequesterUserId());
        recordHistory(req.getId(), req.getStatus(), req.getStatus(), runnerUserId,
                "No-show recorded: " + (reason != null ? reason : "Recipient unreachable"));
    }

    private void publishReadyForDispatch(DeliveryRequest req) {
        eventPublisher.publishEvent(new DeliveryReadyForDispatchEvent(
                req.getId(),
                req.getTrackingCode(),
                req.getCampusId(),
                req.getPaymentMethod(),
                req.getPickupLabel(),
                req.getPickupLat(),
                req.getPickupLng(),
                req.getDropoffLabel(),
                req.getDropoffLat(),
                req.getDropoffLng(),
                req.getRunnerPayoutPesewas(),
                Instant.now()
        ));
    }

    private DeliveryResponse toResponse(DeliveryRequest req) {
        return DeliveryResponse.from(req, null);
    }

    private void recordHistory(
            UUID deliveryId,
            DeliveryStatus from,
            DeliveryStatus to,
            UUID actorUserId,
            String reason
    ) {
        historyRepository.save(new DeliveryStatusHistory(deliveryId, from, to, actorUserId, reason));
    }

    private DeliveryRequest requireDelivery(UUID id) {
        return deliveryRequestRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("DELIVERY_NOT_FOUND", "Delivery request not found"));
    }
}
