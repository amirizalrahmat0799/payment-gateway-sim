CREATE TABLE settlements (
    id               UUID PRIMARY KEY,
    merchant_id      UUID         NOT NULL,
    currency         VARCHAR(3)   NOT NULL,
    settlement_date  DATE         NOT NULL,
    gross_captured   BIGINT       NOT NULL,
    gross_refunded   BIGINT       NOT NULL,
    fees             BIGINT       NOT NULL,
    net_amount       BIGINT       NOT NULL,
    entry_count      INTEGER      NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_settlements_merchant_date ON settlements (merchant_id, settlement_date DESC);

-- Primary key = event id: a redelivered Kafka event hits ON CONFLICT DO NOTHING (idempotent consumer).
CREATE TABLE ledger_entries (
    id             UUID PRIMARY KEY,
    payment_id     UUID         NOT NULL,
    merchant_id    UUID         NOT NULL,
    entry_type     VARCHAR(10)  NOT NULL,
    amount         BIGINT       NOT NULL,
    currency       VARCHAR(3)   NOT NULL,
    fee            BIGINT       NOT NULL,
    occurred_at    TIMESTAMPTZ  NOT NULL,
    settlement_id  UUID REFERENCES settlements (id),
    created_at     TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_ledger_unsettled ON ledger_entries (merchant_id, currency, occurred_at) WHERE settlement_id IS NULL;
