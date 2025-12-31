-- Schema: catalog_configuration
CREATE SCHEMA IF NOT EXISTS catalog_configuration;

-- Product Templates
CREATE TABLE catalog_configuration.product_templates (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    product_family VARCHAR(50) NOT NULL,
    min_width_cm INT NOT NULL DEFAULT 50,
    max_width_cm INT NOT NULL DEFAULT 400,
    min_height_cm INT NOT NULL DEFAULT 50,
    max_height_cm INT NOT NULL DEFAULT 400,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_product_templates_family ON catalog_configuration.product_templates(product_family);
CREATE INDEX idx_product_templates_status ON catalog_configuration.product_templates(status);

-- Option Groups
CREATE TABLE catalog_configuration.option_groups (
    id UUID PRIMARY KEY,
    product_template_id UUID NOT NULL REFERENCES catalog_configuration.product_templates(id),
    name VARCHAR(100) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    required BOOLEAN NOT NULL DEFAULT false,
    multi_select BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_option_groups_template ON catalog_configuration.option_groups(product_template_id);

-- Options
CREATE TABLE catalog_configuration.options (
    id UUID PRIMARY KEY,
    option_group_id UUID NOT NULL REFERENCES catalog_configuration.option_groups(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    sku_code VARCHAR(50),
    display_order INT NOT NULL DEFAULT 0,
    default_selected BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_options_group ON catalog_configuration.options(option_group_id);

-- Configuration Rule Sets
CREATE TABLE catalog_configuration.configuration_rule_sets (
    id UUID PRIMARY KEY,
    product_template_id UUID NOT NULL REFERENCES catalog_configuration.product_templates(id),
    version INT NOT NULL DEFAULT 1,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rule_sets_template ON catalog_configuration.configuration_rule_sets(product_template_id);

-- Configuration Rules
CREATE TABLE catalog_configuration.configuration_rules (
    id UUID PRIMARY KEY,
    rule_set_id UUID NOT NULL REFERENCES catalog_configuration.configuration_rule_sets(id),
    rule_type VARCHAR(30) NOT NULL,
    source_option_id UUID REFERENCES catalog_configuration.options(id),
    target_option_id UUID REFERENCES catalog_configuration.options(id),
    condition_expression TEXT,
    error_code VARCHAR(50) NOT NULL,
    error_message TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rules_set ON catalog_configuration.configuration_rules(rule_set_id);
CREATE INDEX idx_rules_type ON catalog_configuration.configuration_rules(rule_type);

-- BOM Templates
CREATE TABLE catalog_configuration.bom_templates (
    id UUID PRIMARY KEY,
    product_template_id UUID NOT NULL REFERENCES catalog_configuration.product_templates(id),
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- BOM Lines
CREATE TABLE catalog_configuration.bom_lines (
    id UUID PRIMARY KEY,
    bom_template_id UUID NOT NULL REFERENCES catalog_configuration.bom_templates(id),
    sku VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    quantity_formula VARCHAR(200) NOT NULL DEFAULT '1',
    condition_expression TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bom_lines_template ON catalog_configuration.bom_lines(bom_template_id);

-- Catalog Versions (for audit/rollback)
CREATE TABLE catalog_configuration.catalog_versions (
    id UUID PRIMARY KEY,
    version_number INT NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    published_by VARCHAR(100),
    snapshot JSONB NOT NULL,
    UNIQUE(version_number)
);
