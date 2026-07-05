CREATE TABLE IF NOT EXISTS drivers (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL UNIQUE,
    name            VARCHAR(100) NOT NULL,
    phone           VARCHAR(30)  NOT NULL,
    vehicle_plate   VARCHAR(20),
    status          VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE'
);

CREATE TABLE IF NOT EXISTS deliveries (
    id                BIGSERIAL PRIMARY KEY,
    order_id          BIGINT       NOT NULL UNIQUE,
    driver_id         BIGINT,
    delivery_address  VARCHAR(255) NOT NULL,
    status            VARCHAR(30)  NOT NULL DEFAULT 'PENDING_ASSIGNMENT',
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP    NOT NULL,
    CONSTRAINT fk_delivery_driver FOREIGN KEY (driver_id) REFERENCES drivers(id)
);

CREATE INDEX idx_deliveries_order_id ON deliveries(order_id);
CREATE INDEX idx_deliveries_driver_id ON deliveries(driver_id);
CREATE INDEX idx_drivers_status ON drivers(status);