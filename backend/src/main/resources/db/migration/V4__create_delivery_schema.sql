-- V4: Delivery Requests & WhatsApp Payment Links schema (Owned by Member 4)
-- Mirrors docs/schema.dbml: delivery.delivery_requests, delivery.payment_links, delivery.delivery_status_history

CREATE TABLE IF NOT EXISTS delivery.delivery_requests (
    id                      UUID          PRIMARY KEY,
    tracking_code           VARCHAR(32)   NOT NULL UNIQUE,
    campus_id               UUID          NOT NULL,
    requester_user_id       UUID          NOT NULL,
    source                  VARCHAR(32)   NOT NULL,
    package_category        VARCHAR(32)   NOT NULL,
    package_description     VARCHAR(255)  NOT NULL,

    pickup_landmark_id      UUID,
    pickup_zone_id          UUID          NOT NULL,
    pickup_label            VARCHAR(200)  NOT NULL,
    pickup_lat              DECIMAL(9,6)  NOT NULL,
    pickup_lng              DECIMAL(9,6)  NOT NULL,
    pickup_contact_name     VARCHAR(120)  NOT NULL,
    pickup_contact_phone    VARCHAR(20)   NOT NULL,
    pickup_notes            VARCHAR(255),

    dropoff_landmark_id     UUID,
    dropoff_zone_id         UUID          NOT NULL,
    dropoff_label           VARCHAR(200)  NOT NULL,
    dropoff_lat             DECIMAL(9,6)  NOT NULL,
    dropoff_lng             DECIMAL(9,6)  NOT NULL,
    recipient_name          VARCHAR(120)  NOT NULL,
    recipient_phone         VARCHAR(20)   NOT NULL,
    room_notes              VARCHAR(255),

    payment_method          VARCHAR(32)   NOT NULL DEFAULT 'MOMO_PREPAID',
    payment_timing          VARCHAR(32)   NOT NULL DEFAULT 'PREPAID',
    cash_collection_status  VARCHAR(32)   NOT NULL DEFAULT 'NOT_APPLICABLE',
    cash_confirmed_at       TIMESTAMPTZ,
    delivery_fee_pesewas    BIGINT        NOT NULL CHECK (delivery_fee_pesewas > 0),
    runner_payout_pesewas   BIGINT        NOT NULL CHECK (runner_payout_pesewas >= 0),
    platform_cut_pesewas    BIGINT        NOT NULL CHECK (platform_cut_pesewas >= 0),

    pickup_code             VARCHAR(6)    NOT NULL,
    delivery_pin            VARCHAR(6)    NOT NULL,
    pickup_photo_url        VARCHAR(500),
    dropoff_photo_url       VARCHAR(500),

    status                  VARCHAR(32)   NOT NULL DEFAULT 'AWAITING_PAYMENT',
    idempotency_key         VARCHAR(128)  NOT NULL UNIQUE,
    version                 BIGINT        NOT NULL DEFAULT 0,
    created_at              TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_delivery_split_sums CHECK (runner_payout_pesewas + platform_cut_pesewas = delivery_fee_pesewas)
);

CREATE INDEX IF NOT EXISTS idx_delivery_requests_requester ON delivery.delivery_requests (requester_user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_delivery_requests_campus_status ON delivery.delivery_requests (campus_id, status);
CREATE INDEX IF NOT EXISTS idx_delivery_requests_campus_payment_status ON delivery.delivery_requests (campus_id, payment_method, status);
CREATE INDEX IF NOT EXISTS idx_delivery_requests_recipient_phone ON delivery.delivery_requests (recipient_phone);

CREATE TABLE IF NOT EXISTS delivery.payment_links (
    id                  UUID        PRIMARY KEY,
    delivery_request_id UUID        NOT NULL UNIQUE REFERENCES delivery.delivery_requests(id) ON DELETE CASCADE,
    short_code          VARCHAR(24) NOT NULL UNIQUE,
    payer_phone         VARCHAR(20),
    amount_pesewas      BIGINT      NOT NULL CHECK (amount_pesewas > 0),
    status              VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    expires_at          TIMESTAMPTZ NOT NULL,
    paid_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS delivery.delivery_status_history (
    id                  UUID         PRIMARY KEY,
    delivery_request_id UUID         NOT NULL REFERENCES delivery.delivery_requests(id) ON DELETE CASCADE,
    from_status         VARCHAR(32),
    to_status           VARCHAR(32)  NOT NULL,
    changed_by_user_id  UUID,
    reason              VARCHAR(255),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_delivery_history_request ON delivery.delivery_status_history (delivery_request_id, created_at ASC);
