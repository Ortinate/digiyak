package com.digiyak.delivery.api;

import com.digiyak.delivery.api.dto.DeliveryDtos.CancelDeliveryRequest;
import com.digiyak.delivery.api.dto.DeliveryDtos.CreateDeliveryRequest;
import com.digiyak.delivery.api.dto.DeliveryDtos.DeliveryHistoryResponse;
import com.digiyak.delivery.api.dto.DeliveryDtos.DeliveryResponse;
import com.digiyak.delivery.api.dto.DeliveryDtos.PaymentLinkResponse;
import com.digiyak.delivery.api.dto.DeliveryDtos.PublicTrackingResponse;
import com.digiyak.delivery.internal.DeliveryService;
import com.digiyak.shared.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deliveries")
@Tag(name = "Deliveries", description = "Campus walking delivery requests across MoMo Prepaid, Pay-After-Delivery & Physical Cash")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a campus walking delivery request (idempotent)")
    public DeliveryResponse create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateDeliveryRequest request
    ) {
        return deliveryService.createDelivery(user, request, idempotencyKey);
    }

    @GetMapping("/mine")
    @Operation(summary = "List deliveries requested by the authenticated student or vendor")
    public Page<DeliveryResponse> mine(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return deliveryService.listMyDeliveries(user.userId(), pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full delivery details including Pickup Code & Delivery PIN (owner or campus/super admin)")
    public DeliveryResponse getById(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID id
    ) {
        return deliveryService.getDeliveryForActor(user, id);
    }

    @GetMapping("/track/{trackingCode}")
    @Operation(summary = "Public live-tracking projection by tracking code (e.g. DYK-84920)")
    public PublicTrackingResponse track(@PathVariable String trackingCode) {
        return deliveryService.trackByCode(trackingCode);
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Fetch full status transition timeline for a delivery")
    public List<DeliveryHistoryResponse> history(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID id
    ) {
        return deliveryService.getHistory(user, id);
    }

    @PostMapping("/{id}/payment-link")
    @Operation(summary = "Generate or fetch a 1-tap WhatsApp payment link for this delivery")
    public PaymentLinkResponse createPaymentLink(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID id
    ) {
        return deliveryService.createOrGetPaymentLink(user, id);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a delivery before transit")
    public DeliveryResponse cancel(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID id,
            @RequestBody(required = false) CancelDeliveryRequest request
    ) {
        return deliveryService.cancelDelivery(user, id, request != null ? request.reason() : null);
    }
}
