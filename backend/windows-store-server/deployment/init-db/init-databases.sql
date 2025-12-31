-- ============================================================================
-- Kanokna - Database Initialization Script
-- ============================================================================
-- MODULE_CONTRACT: MC-infra-docker-compose
--
-- This script creates all databases and schemas required by Kanokna
-- microservices. It runs automatically when PostgreSQL starts for the
-- first time.
--
-- Services and their databases:
--   - catalog: Product catalog and configuration
--   - pricing: Pricing rules and calculations
--   - cart: Shopping cart management
--   - orders: Order processing
--   - accounts: Customer accounts
--   - media: Media file metadata
--   - notifications: Notification templates and logs
--   - reporting: Analytics and reports
--   - installations: Installation scheduling
--   - keycloak: Identity management (Keycloak)
--
-- Links:
--   - DevelopmentPlan.xml#DP-INFRA-docker-compose
--   - Technology.xml#TECH-DB-001
-- ============================================================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ═══════════════════════════════════════════════════════════════════════════
-- Create Databases
-- ═══════════════════════════════════════════════════════════════════════════

-- Catalog Configuration Service Database
CREATE DATABASE catalog
    WITH OWNER = kanokna
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0
    CONNECTION LIMIT = -1;

COMMENT ON DATABASE catalog IS 'Product catalog and configuration service database';

-- Pricing Service Database
CREATE DATABASE pricing
    WITH OWNER = kanokna
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0
    CONNECTION LIMIT = -1;

COMMENT ON DATABASE pricing IS 'Pricing rules and calculations service database';

-- Cart Service Database
CREATE DATABASE cart
    WITH OWNER = kanokna
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0
    CONNECTION LIMIT = -1;

COMMENT ON DATABASE cart IS 'Shopping cart service database';

-- Order Service Database
CREATE DATABASE orders
    WITH OWNER = kanokna
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0
    CONNECTION LIMIT = -1;

COMMENT ON DATABASE orders IS 'Order processing service database';

-- Account Service Database
CREATE DATABASE accounts
    WITH OWNER = kanokna
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0
    CONNECTION LIMIT = -1;

COMMENT ON DATABASE accounts IS 'Customer account service database';

-- Media Service Database
CREATE DATABASE media
    WITH OWNER = kanokna
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0
    CONNECTION LIMIT = -1;

COMMENT ON DATABASE media IS 'Media file metadata service database';

-- Notification Service Database
CREATE DATABASE notifications
    WITH OWNER = kanokna
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0
    CONNECTION LIMIT = -1;

COMMENT ON DATABASE notifications IS 'Notification service database';

-- Reporting Service Database
CREATE DATABASE reporting
    WITH OWNER = kanokna
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0
    CONNECTION LIMIT = -1;

COMMENT ON DATABASE reporting IS 'Reporting and analytics service database';

-- Installation Service Database
CREATE DATABASE installations
    WITH OWNER = kanokna
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0
    CONNECTION LIMIT = -1;

COMMENT ON DATABASE installations IS 'Installation scheduling service database';

-- Keycloak Database (Identity Management)
CREATE DATABASE keycloak
    WITH OWNER = kanokna
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0
    CONNECTION LIMIT = -1;

COMMENT ON DATABASE keycloak IS 'Keycloak identity management database';

-- ═══════════════════════════════════════════════════════════════════════════
-- Grant Privileges
-- ═══════════════════════════════════════════════════════════════════════════

GRANT ALL PRIVILEGES ON DATABASE catalog TO kanokna;
GRANT ALL PRIVILEGES ON DATABASE pricing TO kanokna;
GRANT ALL PRIVILEGES ON DATABASE cart TO kanokna;
GRANT ALL PRIVILEGES ON DATABASE orders TO kanokna;
GRANT ALL PRIVILEGES ON DATABASE accounts TO kanokna;
GRANT ALL PRIVILEGES ON DATABASE media TO kanokna;
GRANT ALL PRIVILEGES ON DATABASE notifications TO kanokna;
GRANT ALL PRIVILEGES ON DATABASE reporting TO kanokna;
GRANT ALL PRIVILEGES ON DATABASE installations TO kanokna;
GRANT ALL PRIVILEGES ON DATABASE keycloak TO kanokna;

-- ═══════════════════════════════════════════════════════════════════════════
-- Create Schemas in Each Database
-- ═══════════════════════════════════════════════════════════════════════════

-- Note: These commands require connecting to each database individually.
-- PostgreSQL docker-entrypoint-initdb.d scripts run against the default
-- database. The following is for documentation purposes.
-- Actual schema creation will be done by Flyway migrations in each service.

-- Catalog Service Schemas (in catalog database):
--   - public: Core catalog entities
--   - product_config: Product configuration templates

-- Pricing Service Schemas (in pricing database):
--   - public: Pricing rules
--   - promotions: Promotional pricing

-- Cart Service Schemas (in cart database):
--   - public: Cart data

-- Order Service Schemas (in orders database):
--   - public: Order processing
--   - payments: Payment records

-- Account Service Schemas (in accounts database):
--   - public: Customer accounts
--   - preferences: User preferences

-- Media Service Schemas (in media database):
--   - public: Media metadata

-- Notification Service Schemas (in notifications database):
--   - public: Notification logs
--   - templates: Email/SMS templates

-- Reporting Service Schemas (in reporting database):
--   - public: Report definitions
--   - analytics: Aggregated analytics data

-- Installation Service Schemas (in installations database):
--   - public: Installation scheduling
--   - installers: Installer assignments

-- ═══════════════════════════════════════════════════════════════════════════
-- Enable Extensions in Each Database
-- ═══════════════════════════════════════════════════════════════════════════

\c catalog
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

\c pricing
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

\c cart
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

\c orders
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

\c accounts
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

\c media
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

\c notifications
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

\c reporting
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

\c installations
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

\c keycloak
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ═══════════════════════════════════════════════════════════════════════════
-- Initialization Complete
-- ═══════════════════════════════════════════════════════════════════════════

\c kanokna
SELECT 'Kanokna database initialization completed successfully!' AS status;
