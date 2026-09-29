CREATE TABLE card_tokens (
    token            VARCHAR(40)  PRIMARY KEY,
    encrypted_pan    TEXT         NOT NULL,
    fingerprint      VARCHAR(64)  NOT NULL,
    last4            VARCHAR(4)   NOT NULL,
    brand            VARCHAR(20)  NOT NULL,
    expiry_month     INTEGER      NOT NULL,
    expiry_year      INTEGER      NOT NULL,
    cardholder_name  VARCHAR(120),
    created_at       TIMESTAMPTZ  NOT NULL
);

-- Lets us recognise the same card across tokens without decrypting anything.
CREATE INDEX idx_card_tokens_fingerprint ON card_tokens (fingerprint);
