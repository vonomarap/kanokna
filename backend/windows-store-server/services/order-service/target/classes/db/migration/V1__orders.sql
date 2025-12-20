CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    customer_id UUID NULL,
    cart_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    currency CHAR(3) NOT NULL,
    items_json TEXT NOT NULL,
    totals_json TEXT NOT NULL,
    shipping_json TEXT NULL,
    installation_json TEXT NULL,
    paid_total DECIMAL(19,2) NOT NULL DEFAULT 0,
    version BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS payment (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency CHAR(3) NOT NULL,
    method VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    external_ref VARCHAR(255),
    idempotency_key VARCHAR(255)
);
