CREATE TABLE IF NOT EXISTS orders (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT        NOT NULL,
    restaurant_id     BIGINT        NOT NULL,
    status            VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    total_amount      NUMERIC(10,2) NOT NULL,
    delivery_address  VARCHAR(255)  NOT NULL,
    created_at        TIMESTAMP     NOT NULL,
    updated_at        TIMESTAMP     NOT NULL
);

CREATE TABLE IF NOT EXISTS order_items (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT        NOT NULL,
    menu_item_id    BIGINT        NOT NULL,
    menu_item_name  VARCHAR(150)  NOT NULL,
    unit_price      NUMERIC(10,2) NOT NULL,
    quantity        INTEGER       NOT NULL,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_restaurant_id ON orders(restaurant_id);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
