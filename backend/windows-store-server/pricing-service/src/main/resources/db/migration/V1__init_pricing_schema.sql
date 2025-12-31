-- Schema: pricing
CREATE SCHEMA IF NOT EXISTS pricing;

-- Price Books
CREATE TABLE pricing.price_books (
    id UUID PRIMARY KEY,
    product_template_id VARCHAR(100) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'RUB',
    price_per_m2 DECIMAL(12, 2) NOT NULL,
    minimum_area_m2 DECIMAL(6, 4) NOT NULL DEFAULT 0.25,
    minimum_charge DECIMAL(12, 2),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_price_books_product ON pricing.price_books(product_template_id);
CREATE INDEX idx_price_books_status ON pricing.price_books(status);
CREATE UNIQUE INDEX idx_price_books_active ON pricing.price_books(product_template_id, currency)
    WHERE status = 'ACTIVE';

-- Option Premiums
CREATE TABLE pricing.option_premiums (
    id UUID PRIMARY KEY,
    price_book_id UUID NOT NULL REFERENCES pricing.price_books(id),
    option_id VARCHAR(100) NOT NULL,
    option_name VARCHAR(200) NOT NULL,
    premium_type VARCHAR(20) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_option_premiums_pricebook ON pricing.option_premiums(price_book_id);

-- Campaigns
CREATE TABLE pricing.campaigns (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(10, 2) NOT NULL,
    max_discount DECIMAL(12, 2),
    applicable_products JSONB,
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    priority INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100)
);

CREATE INDEX idx_campaigns_status ON pricing.campaigns(status);
CREATE INDEX idx_campaigns_dates ON pricing.campaigns(start_date, end_date);

-- Promo Codes
CREATE TABLE pricing.promo_codes (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(10, 2) NOT NULL,
    max_discount DECIMAL(12, 2),
    min_subtotal DECIMAL(12, 2),
    usage_limit INT,
    usage_count INT NOT NULL DEFAULT 0,
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date TIMESTAMP WITH TIME ZONE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100)
);

CREATE INDEX idx_promo_codes_code ON pricing.promo_codes(code);
CREATE INDEX idx_promo_codes_active ON pricing.promo_codes(active, start_date, end_date);

-- Tax Rules
CREATE TABLE pricing.tax_rules (
    id UUID PRIMARY KEY,
    region VARCHAR(10) NOT NULL UNIQUE,
    region_name VARCHAR(100) NOT NULL,
    tax_rate_percent DECIMAL(5, 2) NOT NULL,
    tax_type VARCHAR(20) NOT NULL DEFAULT 'VAT',
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tax_rules_region ON pricing.tax_rules(region);

-- Price Book Versions (for audit/history)
CREATE TABLE pricing.price_book_versions (
    id UUID PRIMARY KEY,
    price_book_id UUID NOT NULL REFERENCES pricing.price_books(id),
    version_number INT NOT NULL,
    snapshot JSONB NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    published_by VARCHAR(100),
    UNIQUE(price_book_id, version_number)
);

CREATE INDEX idx_price_book_versions_pricebook ON pricing.price_book_versions(price_book_id);

-- Quote Audit Log (optional, for analytics)
CREATE TABLE pricing.quote_audit_log (
    id UUID PRIMARY KEY,
    quote_id VARCHAR(100) NOT NULL,
    product_template_id VARCHAR(100) NOT NULL,
    customer_id VARCHAR(100),
    base_price DECIMAL(12, 2) NOT NULL,
    total DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    promo_code VARCHAR(50),
    discount DECIMAL(12, 2),
    decision_trace JSONB,
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_quote_audit_product ON pricing.quote_audit_log(product_template_id);
CREATE INDEX idx_quote_audit_date ON pricing.quote_audit_log(calculated_at);
