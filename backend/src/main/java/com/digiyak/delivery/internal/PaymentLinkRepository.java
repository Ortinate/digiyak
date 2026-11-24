package com.digiyak.delivery.internal;

import com.digiyak.delivery.domain.PaymentLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentLinkRepository extends JpaRepository<PaymentLink, UUID> {

    Optional<PaymentLink> findByShortCode(String shortCode);

    Optional<PaymentLink> findByDeliveryRequestId(UUID deliveryRequestId);
}
