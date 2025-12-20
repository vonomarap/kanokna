CREATE TABLE IF NOT EXISTS price_book (
    id UUID PRIMARY KEY,
    region VARCHAR(50) NOT NULL,
    currency CHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    version BIGINT NOT NULL,
    base_prices_json TEXT NOT NULL,
    option_premiums_json TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS campaign (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    percent_off DECIMAL(5,3) NOT NULL,
    starts_at TIMESTAMP NULL,
    ends_at TIMESTAMP NULL,
    conditions_json TEXT
);

CREATE TABLE IF NOT EXISTS tax_rule (
    id UUID PRIMARY KEY,
    region VARCHAR(50) NOT NULL,
    product_type VARCHAR(50),
    rate DECIMAL(5,3) NOT NULL,
    rounding_policy VARCHAR(20) NOT NULL
);
