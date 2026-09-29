CREATE TABLE payments (
    id               UUID PRIMARY KEY,
    merchant_id      UUID         NOT NULL,
    amount           BIGINT       NOT NULL CHECK (amount > 0),
    currency         VARCHAR(3)   NOT NULL,
    captured_amount  BIGINT       NOT NULL DEFAULT 0,
    refunded_amount  BIGINT       NOT NULL DEFAULT 0,
    status           VARCHAR(20)  NOT NULL,
    card_token       VARCHAR(40)  NOT NULL,
    card_brand       VARCHAR(20)  NOT NULL,
    card_last4       VARCHAR(4)   NOT NULL,
    fee_bps          INTEGER      NOT NULL,
    auth_code        VARCHAR(12),
    decline_reason   VARCHAR(40),
    description      VARCHAR(255),
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL,
    version          BIGINT       NOT NULL
);

CREATE INDEX idx_payments_merchant_created ON payments (merchant_id, created_at DESC);

CREATE TABLE refunds (
    id          UUID PRIMARY KEY,
    payment_id  UUID        NOT NULL REFERENCES payments (id),
    amount      BIGINT      NOT NULL CHECK (amount > 0),
    created_at  TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_refunds_payment ON refunds (payment_id);

-- A retried request with the same (merchant, key) must return the original payment.
CREATE TABLE idempotency_keys (
    id               UUID PRIMARY KEY,
    merchant_id      UUID         NOT NULL,
    idempotency_key  VARCHAR(100) NOT NULL,
    request_hash     VARCHAR(64)  NOT NULL,
    payment_id       UUID         NOT NULL REFERENCES payments (id),
    created_at       TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_idempotency_merchant_key UNIQUE (merchant_id, idempotency_key)
);

-- Transactional outbox: events are written in the same DB transaction as the state change,
-- then relayed to Kafka by a poller. No lost events, no dual-write problem.
CREATE TABLE outbox_events (
    id            UUID PRIMARY KEY,
    aggregate_id  UUID         NOT NULL,
    topic         VARCHAR(100) NOT NULL,
    event_key     VARCHAR(100) NOT NULL,
    payload       TEXT         NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL,
    published_at  TIMESTAMPTZ,
    attempts      INTEGER      NOT NULL DEFAULT 0
);

CREATE INDEX idx_outbox_unpublished ON outbox_events (created_at) WHERE published_at IS NULL;
