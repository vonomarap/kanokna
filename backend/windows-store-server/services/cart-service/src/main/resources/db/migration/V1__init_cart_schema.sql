CREATE SCHEMA IF NOT EXISTS cart;

CREATE TABLE IF NOT EXISTS cart.carts (
    cart_id UUID PRIMARY KEY,
    customer_id VARCHAR(255),
    session_id VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    applied_promo_code VARCHAR(50),
    promo_discount_amount DECIMAL(19,4),
    promo_discount_currency VARCHAR(3),
    subtotal_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    subtotal_currency VARCHAR(3) NOT NULL DEFAULT 'RUB',
    discount_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    total_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_cart_owner CHECK (customer_id IS NOT NULL OR session_id IS NOT NULL)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_carts_customer
    ON cart.carts (customer_id)
    WHERE customer_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_carts_session
    ON cart.carts (session_id)
    WHERE session_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_carts_status
    ON cart.carts (status);

CREATE INDEX IF NOT EXISTS idx_carts_updated
    ON cart.carts (updated_at);

CREATE TABLE IF NOT EXISTS cart.cart_items (
    item_id UUID PRIMARY KEY,
    cart_id UUID NOT NULL REFERENCES cart.carts (cart_id) ON DELETE CASCADE,    
    product_template_id VARCHAR(255) NOT NULL,
    product_name VARCHAR(500) NOT NULL,
    product_family VARCHAR(50),
    thumbnail_url TEXT,
    configuration_snapshot JSONB NOT NULL,
    configuration_hash VARCHAR(64) NOT NULL,
    quantity INT NOT NULL CHECK (quantity >= 1),
    unit_price_amount DECIMAL(19,4) NOT NULL,
    unit_price_currency VARCHAR(3) NOT NULL,
    line_total_amount DECIMAL(19,4) NOT NULL,
    quote_id VARCHAR(255),
    quote_valid_until TIMESTAMPTZ,
    validation_status VARCHAR(20) NOT NULL DEFAULT 'VALID',
    validation_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_cart_items_cart
    ON cart.cart_items (cart_id);

CREATE INDEX IF NOT EXISTS idx_cart_items_product
    ON cart.cart_items (product_template_id);

CREATE INDEX IF NOT EXISTS idx_cart_items_hash
    ON cart.cart_items (cart_id, configuration_hash);

CREATE TABLE IF NOT EXISTS cart.cart_snapshots (
    snapshot_id UUID PRIMARY KEY,
    cart_id UUID NOT NULL,
    customer_id VARCHAR(255) NOT NULL,
    snapshot_data JSONB NOT NULL,
    subtotal_amount DECIMAL(19,4) NOT NULL,
    discount_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    total_amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    applied_promo_code VARCHAR(50),
    item_count INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    valid_until TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_cart_snapshots_customer
    ON cart.cart_snapshots (customer_id);

CREATE INDEX IF NOT EXISTS idx_cart_snapshots_valid
    ON cart.cart_snapshots (valid_until);
