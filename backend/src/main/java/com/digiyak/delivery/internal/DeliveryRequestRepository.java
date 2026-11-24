package com.digiyak.delivery.internal;

import com.digiyak.delivery.domain.DeliveryEnums.DeliveryStatus;
import com.digiyak.delivery.domain.DeliveryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryRequestRepository extends JpaRepository<DeliveryRequest, UUID> {

    Optional<DeliveryRequest> findByTrackingCode(String trackingCode);

    Optional<DeliveryRequest> findByIdempotencyKey(String idempotencyKey);

    Page<DeliveryRequest> findByRequesterUserIdOrderByCreatedAtDesc(UUID requesterUserId, Pageable pageable);

    Page<DeliveryRequest> findByCampusIdAndStatusOrderByCreatedAtDesc(
            UUID campusId,
            DeliveryStatus status,
            Pageable pageable
    );
}
