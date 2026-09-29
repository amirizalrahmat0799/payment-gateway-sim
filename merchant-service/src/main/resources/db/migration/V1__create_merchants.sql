CREATE TABLE merchants (
    id              UUID PRIMARY KEY,
    name            VARCHAR(120) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    status          VARCHAR(20)  NOT NULL,
    fee_bps         INTEGER      NOT NULL,
    api_key_prefix  VARCHAR(16)  NOT NULL,
    api_key_hash    VARCHAR(64)  NOT NULL UNIQUE,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL
);
