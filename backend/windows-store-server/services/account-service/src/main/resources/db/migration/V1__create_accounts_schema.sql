CREATE SCHEMA IF NOT EXISTS accounts;

CREATE TABLE IF NOT EXISTS accounts.user_profiles (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    preferred_language VARCHAR(10),
    preferred_currency CHAR(3),
    notification_preferences JSONB,
    partner_organization_id UUID,
    version INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS accounts.saved_addresses (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    label VARCHAR(50) NOT NULL,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country CHAR(2) NOT NULL,
    region VARCHAR(100),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_address_no_dup UNIQUE (user_id, street, city, postal_code),
    CONSTRAINT fk_saved_addresses_user FOREIGN KEY (user_id)
        REFERENCES accounts.user_profiles (id)
);

CREATE INDEX IF NOT EXISTS idx_saved_addresses_user_id
    ON accounts.saved_addresses (user_id);

CREATE TABLE IF NOT EXISTS accounts.saved_configurations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    product_template_id UUID NOT NULL,
    configuration_snapshot JSONB NOT NULL,
    quote_snapshot JSONB,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_saved_configurations_user FOREIGN KEY (user_id)
        REFERENCES accounts.user_profiles (id)
);

CREATE INDEX IF NOT EXISTS idx_saved_configurations_user_id
    ON accounts.saved_configurations (user_id);