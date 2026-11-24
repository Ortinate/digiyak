package com.digiyak.delivery.internal;

import com.digiyak.delivery.api.dto.DeliveryDtos.PaymentLinkResponse;
import com.digiyak.delivery.domain.DeliveryEnums.PayLinkStatus;
import com.digiyak.delivery.domain.DeliveryRequest;
import com.digiyak.delivery.domain.PaymentLink;
import com.digiyak.shared.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class PaymentLinkService {

    private final PaymentLinkRepository paymentLinkRepository;
    private final DeliveryRequestRepository deliveryRequestRepository;

    public PaymentLinkService(
            PaymentLinkRepository paymentLinkRepository,
            DeliveryRequestRepository deliveryRequestRepository
    ) {
        this.paymentLinkRepository = paymentLinkRepository;
        this.deliveryRequestRepository = deliveryRequestRepository;
    }

    public PaymentLink getOrCreateForDelivery(DeliveryRequest request) {
        return paymentLinkRepository.findByDeliveryRequestId(request.getId())
                .orElseGet(() -> paymentLinkRepository.save(
                        PaymentLink.create(
                                request.getId(),
                                request.getRecipientPhone(),
                                request.getDeliveryFeePesewas()
                        )
                ));
    }

    @Transactional(readOnly = true)
    public PaymentLinkResponse getGuestCheckoutDetails(String shortCode) {
        PaymentLink link = paymentLinkRepository.findByShortCode(shortCode)
                .orElseThrow(() -> ApiException.notFound("PAYMENT_LINK_NOT_FOUND", "Payment link not found"));

        if (link.getStatus() == PayLinkStatus.ACTIVE && link.isExpired(Instant.now())) {
            throw ApiException.unprocessable("PAYMENT_LINK_EXPIRED", "This payment link has expired");
        }

        DeliveryRequest delivery = deliveryRequestRepository.findById(link.getDeliveryRequestId())
                .orElseThrow(() -> ApiException.notFound("DELIVERY_NOT_FOUND", "Associated delivery not found"));

        return PaymentLinkResponse.from(link, delivery);
    }

    public void markPaidIfPresent(UUID deliveryRequestId) {
        paymentLinkRepository.findByDeliveryRequestId(deliveryRequestId)
                .ifPresent(link -> {
                    link.markPaid();
                    paymentLinkRepository.save(link);
                });
    }

    public void cancelIfPresent(UUID deliveryRequestId) {
        paymentLinkRepository.findByDeliveryRequestId(deliveryRequestId)
                .ifPresent(link -> {
                    link.cancel();
                    paymentLinkRepository.save(link);
                });
    }
}
