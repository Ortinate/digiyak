-- V2: Identity & Auth schema (Owned by Member 2)
-- Mirrors docs/schema.dbml: identity.users, identity.user_roles, identity.refresh_tokens, identity.admin_audit_logs

CREATE TABLE IF NOT EXISTS identity.users (
    id                      UUID PRIMARY KEY,
    campus_id               UUID,
    email                   VARCHAR(255) NOT NULL UNIQUE,
    phone                   VARCHAR(20)  NOT NULL UNIQUE,
    password_hash           VARCHAR(255) NOT NULL,
    full_name               VARCHAR(150) NOT NULL,
    student_id_number       VARCHAR(64)  UNIQUE,
    business_name           VARCHAR(150),
    momo_provider           VARCHAR(20),
    momo_number             VARCHAR(20),
    paystack_recipient_code VARCHAR(100),
    no_show_count           INTEGER      NOT NULL DEFAULT 0 CHECK (no_show_count >= 0),
    allow_postpaid          BOOLEAN      NOT NULL DEFAULT TRUE,
    is_verified             BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active               BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_identity_users_campus ON identity.users (campus_id);

CREATE TABLE IF NOT EXISTS identity.user_roles (
    user_id    UUID        NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
    role       VARCHAR(32) NOT NULL,
    granted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, role)
);

CREATE TABLE IF NOT EXISTS identity.refresh_tokens (
    id         UUID         PRIMARY KEY,
    user_id    UUID         NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_identity_refresh_tokens_user ON identity.refresh_tokens (user_id);

CREATE TABLE IF NOT EXISTS identity.admin_audit_logs (
    id                 UUID         PRIMARY KEY,
    actor_user_id      UUID         NOT NULL REFERENCES identity.users(id),
    actor_role         VARCHAR(32)  NOT NULL,
    target_campus_id   UUID,
    action             VARCHAR(100) NOT NULL,
    target_entity_type VARCHAR(80)  NOT NULL,
    target_entity_id   UUID,
    metadata_json      TEXT,
    ip_address         VARCHAR(64),
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_identity_audit_actor ON identity.admin_audit_logs (actor_user_id);
CREATE INDEX IF NOT EXISTS idx_identity_audit_campus_created ON identity.admin_audit_logs (target_campus_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_identity_audit_action ON identity.admin_audit_logs (action);
