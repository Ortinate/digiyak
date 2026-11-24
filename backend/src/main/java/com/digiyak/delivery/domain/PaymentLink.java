package com.digiyak.delivery.domain;

import com.digiyak.delivery.domain.DeliveryEnums.PayLinkStatus;
import com.digiyak.shared.ApiException;
import com.digiyak.shared.CodeGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_links", schema = "delivery")
public class PaymentLink {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

    @Id
    private UUID id;

    @Column(name = "delivery_request_id", nullable = false, unique = true)
    private UUID deliveryRequestId;

    @Column(name = "short_code", nullable = false, unique = true, length = 24)
    private String shortCode;

    @Column(name = "payer_phone", length = 20)
    private String payerPhone;

    @Column(name = "amount_pesewas", nullable = false)
    private long amountPesewas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private PayLinkStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PaymentLink() {
    }

    public static PaymentLink create(UUID deliveryRequestId, String payerPhone, long amountPesewas) {
        Instant now = Instant.now();
        PaymentLink link = new PaymentLink();
        link.id = UUID.randomUUID();
        link.deliveryRequestId = deliveryRequestId;
        link.shortCode = CodeGenerator.shortPayLinkCode();
        link.payerPhone = payerPhone;
        link.amountPesewas = amountPesewas;
        link.status = PayLinkStatus.ACTIVE;
        link.createdAt = now;
        link.expiresAt = now.plus(DEFAULT_TTL);
        return link;
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(this.expiresAt);
    }

    public void markPaid() {
        if (this.status == PayLinkStatus.PAID) {
            return;
        }
        if (this.status != PayLinkStatus.ACTIVE) {
            throw ApiException.conflict("PAYMENT_LINK_INACTIVE", "Payment link is no longer active");
        }
        this.status = PayLinkStatus.PAID;
        this.paidAt = Instant.now();
    }

    public void cancel() {
        if (this.status == PayLinkStatus.ACTIVE) {
            this.status = PayLinkStatus.CANCELLED;
        }
    }

    public UUID getId() { return id; }
    public UUID getDeliveryRequestId() { return deliveryRequestId; }
    public String getShortCode() { return shortCode; }
    public String getPayerPhone() { return payerPhone; }
    public long getAmountPesewas() { return amountPesewas; }
    public PayLinkStatus getStatus() { return status; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getPaidAt() { return paidAt; }
    public Instant getCreatedAt() { return createdAt; }
}
