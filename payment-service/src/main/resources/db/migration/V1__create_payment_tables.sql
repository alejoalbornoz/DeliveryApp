CREATE TABLE IF NOT EXISTS payments (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT          NOT NULL UNIQUE,
    user_id         BIGINT          NOT NULL,
    mp_payment_id   VARCHAR(100),
    init_point      VARCHAR(500),
    preference_id   VARCHAR(100),
    amount          NUMERIC(10,2)   NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP       NOT NULL
);

CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_mp_payment_id ON payments(mp_payment_id);