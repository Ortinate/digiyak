package com.digiyak.delivery.domain;

public final class DeliveryEnums {

    private DeliveryEnums() {
    }

    public enum DeliverySource {
        STUDENT_P2P,
        VENDOR_WALLET,
        VENDOR_PAY_LINK
    }

    public enum PackageCategory {
        PREPAID_FOOD,
        DOCUMENTS_BOOKS,
        ELECTRONICS_CHARGER,
        LAUNDRY,
        GATE_PARCEL,
        OTHER
    }

    public enum PaymentMethod {
        MOMO_PREPAID,
        MOMO_PAY_AFTER_DELIVERY,
        PHYSICAL_CASH,
        VENDOR_WALLET;

        public boolean isPostpaid() {
            return this == MOMO_PAY_AFTER_DELIVERY || this == PHYSICAL_CASH;
        }
    }

    public enum PaymentTiming {
        PREPAID,
        PAY_AT_PICKUP,
        PAY_ON_DELIVERY
    }

    public enum CashCollectionStatus {
        NOT_APPLICABLE,
        PENDING_COLLECTION,
        COLLECTED_BY_RUNNER,
        DISPUTED
    }

    public enum DeliveryStatus {
        AWAITING_PAYMENT,
        FINDING_RUNNER,
        RUNNER_ASSIGNED,
        AT_PICKUP,
        IN_TRANSIT,
        AWAITING_HANDOFF_PAYMENT,
        DELIVERED,
        CANCELLED,
        REFUNDED
    }

    public enum PayLinkStatus {
        ACTIVE,
        PAID,
        EXPIRED,
        CANCELLED
    }
}
