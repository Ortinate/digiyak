package com.digiyak.delivery.internal;

import com.digiyak.delivery.domain.DeliveryStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeliveryStatusHistoryRepository extends JpaRepository<DeliveryStatusHistory, UUID> {

    List<DeliveryStatusHistory> findByDeliveryRequestIdOrderByCreatedAtAsc(UUID deliveryRequestId);
}
