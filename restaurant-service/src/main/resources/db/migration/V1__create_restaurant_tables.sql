CREATE TABLE IF NOT EXISTS restaurants (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150)  NOT NULL,
    description VARCHAR(500),
    address     VARCHAR(255)  NOT NULL,
    phone       VARCHAR(30)   NOT NULL,
    owner_id    BIGINT        NOT NULL,
    status      VARCHAR(20)   NOT NULL DEFAULT 'OPEN',
    rating      DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP     NOT NULL
);

CREATE TABLE IF NOT EXISTS categories (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS menu_items (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150)    NOT NULL,
    description     VARCHAR(500),
    price           NUMERIC(10,2)   NOT NULL,
    available       BOOLEAN         NOT NULL DEFAULT TRUE,
    restaurant_id   BIGINT          NOT NULL,
    category_id     BIGINT          NOT NULL,
    CONSTRAINT fk_menu_item_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    CONSTRAINT fk_menu_item_category   FOREIGN KEY (category_id)   REFERENCES categories(id)
);

CREATE INDEX idx_restaurants_owner_id ON restaurants(owner_id);
CREATE INDEX idx_menu_items_restaurant_id ON menu_items(restaurant_id);

-- Seed a few common categories so menu items can be created immediately
INSERT INTO categories (name) VALUES
    ('Pizzas'), ('Burgers'), ('Drinks'), ('Desserts'), ('Salads')
ON CONFLICT (name) DO NOTHING;