-- Development seed data

-- Default tax rule for Russia
INSERT INTO pricing.tax_rules (id, region, region_name, tax_rate_percent, tax_type)
VALUES ('a0000000-0000-0000-0000-000000000001', 'RU', 'Russia', 20.00, 'VAT');

-- Sample price book for windows (requires matching product template in catalog)
-- INSERT INTO pricing.price_books (id, product_template_id, currency, price_per_m2, minimum_area_m2, minimum_charge, status)
-- VALUES ('b0000000-0000-0000-0000-000000000001', 'WINDOW-PVC-STANDARD', 'RUB', 15000.00, 0.25, 5000.00, 'ACTIVE');

-- Sample campaign
-- INSERT INTO pricing.campaigns (id, name, description, discount_type, discount_value, start_date, end_date, status, priority)
-- VALUES ('c0000000-0000-0000-0000-000000000001', 'New Year Sale 2026', '10% off all windows', 'PERCENTAGE', 10, '2026-01-01', '2026-01-31', 'SCHEDULED', 10);

-- Sample promo code
-- INSERT INTO pricing.promo_codes (id, code, description, discount_type, discount_value, usage_limit, start_date, end_date)
-- VALUES ('d0000000-0000-0000-0000-000000000001', 'WELCOME10', 'First order 10% off', 'PERCENTAGE', 10, 1000, '2025-01-01', '2025-12-31');
