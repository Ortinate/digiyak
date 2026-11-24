package com.digiyak.delivery.api;

import com.digiyak.delivery.api.dto.DeliveryDtos.PaymentLinkResponse;
import com.digiyak.delivery.internal.PaymentLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pay")
@Tag(name = "WhatsApp Payment Links", description = "Zero-login guest MoMo checkout for vendor links & Pay-After-Delivery handoffs")
public class PaymentLinkController {

    private final PaymentLinkService paymentLinkService;

    public PaymentLinkController(PaymentLinkService paymentLinkService) {
        this.paymentLinkService = paymentLinkService;
    }

    @GetMapping("/{shortCode}")
    @Operation(summary = "Fetch public guest checkout summary for a WhatsApp or handoff payment link")
    public PaymentLinkResponse getGuestCheckout(@PathVariable String shortCode) {
        return paymentLinkService.getGuestCheckoutDetails(shortCode);
    }
}
