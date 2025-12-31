-- Seed data for development: Basic window and door templates

-- Insert a basic window template
INSERT INTO catalog_configuration.product_templates
    (id, name, description, product_family, min_width_cm, max_width_cm, min_height_cm, max_height_cm, status, version, created_at, updated_at)
VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 'Standard Casement Window', 'Classic casement window with tilt and turn mechanism', 'CASEMENT_WINDOW', 60, 200, 80, 220, 'ACTIVE', 1, NOW(), NOW()),
    ('550e8400-e29b-41d4-a716-446655440002', 'Standard Door', 'Classic single door', 'DOOR', 80, 120, 200, 240, 'ACTIVE', 1, NOW(), NOW());

-- Insert option groups for window
INSERT INTO catalog_configuration.option_groups
    (id, product_template_id, name, display_order, required, multi_select, created_at)
VALUES
    ('650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'Frame Material', 0, true, false, NOW()),
    ('650e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', 'Glazing Type', 1, true, false, NOW()),
    ('650e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440001', 'Color', 2, true, false, NOW());

-- Insert options for Frame Material
INSERT INTO catalog_configuration.options
    (id, option_group_id, name, description, sku_code, display_order, default_selected, created_at)
VALUES
    ('750e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', 'PVC', 'PVC frame - durable and low maintenance', 'FRAME-PVC', 0, true, NOW()),
    ('750e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440001', 'Aluminum', 'Aluminum frame - modern and strong', 'FRAME-ALU', 1, false, NOW()),
    ('750e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440001', 'Wood', 'Wooden frame - classic and elegant', 'FRAME-WOOD', 2, false, NOW());

-- Insert options for Glazing Type
INSERT INTO catalog_configuration.options
    (id, option_group_id, name, description, sku_code, display_order, default_selected, created_at)
VALUES
    ('750e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440002', 'Double Glazing', 'Standard double glazing', 'GLAZE-DOUBLE', 0, true, NOW()),
    ('750e8400-e29b-41d4-a716-446655440005', '650e8400-e29b-41d4-a716-446655440002', 'Triple Glazing', 'Premium triple glazing for better insulation', 'GLAZE-TRIPLE', 1, false, NOW());

-- Insert options for Color
INSERT INTO catalog_configuration.options
    (id, option_group_id, name, description, sku_code, display_order, default_selected, created_at)
VALUES
    ('750e8400-e29b-41d4-a716-446655440006', '650e8400-e29b-41d4-a716-446655440003', 'White', 'Classic white', 'COLOR-WHITE', 0, true, NOW()),
    ('750e8400-e29b-41d4-a716-446655440007', '650e8400-e29b-41d4-a716-446655440003', 'Brown', 'Brown finish', 'COLOR-BROWN', 1, false, NOW()),
    ('750e8400-e29b-41d4-a716-446655440008', '650e8400-e29b-41d4-a716-446655440003', 'Gray', 'Modern gray', 'COLOR-GRAY', 2, false, NOW());

-- Create a basic rule set for the window
INSERT INTO catalog_configuration.configuration_rule_sets
    (id, product_template_id, version, active, created_at)
VALUES
    ('850e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 1, true, NOW());

-- Add a sample BOM template for the window
INSERT INTO catalog_configuration.bom_templates
    (id, product_template_id, version, created_at)
VALUES
    ('950e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 1, NOW());

-- Add BOM lines
INSERT INTO catalog_configuration.bom_lines
    (id, bom_template_id, sku, description, quantity_formula, created_at)
VALUES
    ('a50e8400-e29b-41d4-a716-446655440001', '950e8400-e29b-41d4-a716-446655440001', 'GLASS-PANE', 'Glass pane', '1', NOW()),
    ('a50e8400-e29b-41d4-a716-446655440002', '950e8400-e29b-41d4-a716-446655440001', 'FRAME-PROFILE', 'Frame profile', '2', NOW()),
    ('a50e8400-e29b-41d4-a716-446655440003', '950e8400-e29b-41d4-a716-446655440001', 'SEAL-RUBBER', 'Sealing rubber', '4', NOW());
