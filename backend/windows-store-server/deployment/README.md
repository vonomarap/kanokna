# Kanokna Deployment Configuration

Local development infrastructure for the Kanokna Windows & Doors E-Commerce platform.

---

## MODULE_CONTRACT: MC-infra-docker-compose

```
────────────────────────────────────────────────────────────────────────────────
ID:          MC-infra-docker-compose
Type:        Infrastructure
Version:     1.0.0
Status:      IMPLEMENTED
────────────────────────────────────────────────────────────────────────────────

DESCRIPTION:
  Provides Docker Compose configuration for running all infrastructure
  dependencies required by Kanokna microservices during local development.

SERVICES PROVIDED:
  1. PostgreSQL 16.4  - Primary relational database
  2. Kafka 7.6.0      - Event streaming platform (with Zookeeper)
  3. Redis 7.2.4      - Caching and session storage
  4. Elasticsearch 8.12.2 - Full-text search engine
  5. Keycloak 24.0.1  - Identity and access management
  6. MinIO (latest)   - S3-compatible object storage

OPTIONAL SERVICES (profiles):
  - Kafka UI (tools profile)
  - Redis Commander (tools profile)
  - Kibana (tools profile)
  - Prometheus (monitoring profile)
  - Grafana (monitoring profile)
  - Loki (monitoring profile)

DATABASES CREATED:
  - catalog       - Catalog configuration service
  - pricing       - Pricing service
  - cart          - Cart service
  - orders        - Order service
  - accounts      - Account service
  - media         - Media service
  - notifications - Notification service
  - reporting     - Reporting service
  - installations - Installation service
  - keycloak      - Identity management

LINKS:
  - DevelopmentPlan.xml#DP-INFRA-docker-compose
  - Technology.xml#TECH-INFRA-001
────────────────────────────────────────────────────────────────────────────────
```

---

## Quick Start

### Prerequisites

- Docker Desktop 4.x or later
- Docker Compose V2 (included with Docker Desktop)
- 8GB+ RAM allocated to Docker

### Starting Infrastructure

```bash
# Navigate to deployment directory
cd backend/windows-store-server/deployment

# Copy and customize environment variables (optional)
cp env/.env.example env/.env

# Start core infrastructure
docker compose up -d

# Start with optional tools (Kafka UI, Redis Commander, Kibana)
docker compose --profile tools up -d

# Start with monitoring (Prometheus, Grafana, Loki)
docker compose --profile monitoring up -d

# Start everything
docker compose --profile tools --profile monitoring up -d
```

### Stopping Infrastructure

```bash
# Stop all services
docker compose down

# Stop and remove volumes (CAUTION: destroys data)
docker compose down -v
```

### Viewing Logs

```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f postgres
docker compose logs -f kafka
```

---

## Service Endpoints

### Core Services

| Service | Port | URL | Credentials |
|---------|------|-----|-------------|
| PostgreSQL | 5432 | `jdbc:postgresql://localhost:5432/kanokna` | kanokna / kanokna_secret |
| Kafka Broker | 9092 | `localhost:9092` | - |
| Kafka Internal | 29092 | `kafka:29092` (container) | - |
| Zookeeper | 2181 | `localhost:2181` | - |
| Redis | 6379 | `localhost:6379` | - |
| Elasticsearch | 9200 | `http://localhost:9200` | - |
| Keycloak | 8180 | `http://localhost:8180` | admin / admin |
| MinIO API | 9000 | `http://localhost:9000` | minioadmin / minioadmin123 |
| MinIO Console | 9001 | `http://localhost:9001` | minioadmin / minioadmin123 |

### Optional Tools (--profile tools)

| Service | Port | URL |
|---------|------|-----|
| Kafka UI | 8090 | `http://localhost:8090` |
| Redis Commander | 8091 | `http://localhost:8091` |
| Kibana | 5601 | `http://localhost:5601` |

### Monitoring (--profile monitoring)

| Service | Port | URL | Credentials |
|---------|------|-----|-------------|
| Prometheus | 9090 | `http://localhost:9090` | - |
| Grafana | 3000 | `http://localhost:3000` | admin / admin |
| Loki | 3100 | `http://localhost:3100` | - |

---

## Database Configuration

### Connecting from Services

Each microservice should use these connection parameters:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME}
    username: ${DB_USERNAME:kanokna}
    password: ${DB_PASSWORD:kanokna_secret}
```

### Database per Service

| Service | Database Name | Schema |
|---------|---------------|--------|
| catalog-configuration-service | catalog | public |
| pricing-service | pricing | public |
| cart-service | cart | public |
| order-service | orders | public |
| account-service | accounts | public |
| media-service | media | public |
| notification-service | notifications | public |
| reporting-service | reporting | public |
| installation-service | installations | public |

### Running Migrations

Flyway migrations run automatically when each service starts. To run manually:

```bash
# From service directory
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/catalog
```

---

## Kafka Configuration

### Broker Settings

- Bootstrap Servers: `localhost:9092` (external) / `kafka:29092` (internal)
- Auto-create topics: Enabled
- Default partitions: 3
- Default replication: 1 (single broker)

### Common Topics

```
catalog.product.created
catalog.product.updated
catalog.product.deleted
pricing.price.updated
cart.item.added
cart.item.removed
order.created
order.completed
order.cancelled
notification.send
installation.scheduled
installation.completed
```

### Using Kafka CLI

```bash
# List topics
docker exec -it kanokna-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Create topic
docker exec -it kanokna-kafka kafka-topics --bootstrap-server localhost:9092 \
  --create --topic my-topic --partitions 3 --replication-factor 1

# Produce messages
docker exec -it kanokna-kafka kafka-console-producer \
  --bootstrap-server localhost:9092 --topic my-topic

# Consume messages
docker exec -it kanokna-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 --topic my-topic --from-beginning
```

---

## Redis Configuration

### Connection

```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
```

### CLI Access

```bash
docker exec -it kanokna-redis redis-cli

# Common commands
> PING
> KEYS *
> GET mykey
> SET mykey "value"
```

---

## Elasticsearch Configuration

### Connection

```yaml
spring:
  elasticsearch:
    uris: http://${ELASTICSEARCH_HOST:localhost}:${ELASTICSEARCH_PORT:9200}
```

### API Access

```bash
# Check cluster health
curl http://localhost:9200/_cluster/health?pretty

# List indices
curl http://localhost:9200/_cat/indices?v

# Search
curl http://localhost:9200/products/_search?pretty
```

---

## Keycloak Configuration

### Admin Console

1. Navigate to `http://localhost:8180`
2. Login with `admin / admin`
3. Create realm: `kanokna`
4. Create client: `kanokna-api`

### Service Configuration

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8180/realms/kanokna
```

---

## MinIO Configuration

### Buckets Created Automatically

- `media` - Product images and media files (public read)
- `documents` - PDF documents and specs
- `uploads` - User uploads (temporary)

### Service Configuration

```yaml
aws:
  s3:
    endpoint: http://${S3_ENDPOINT:localhost:9000}
    access-key: ${S3_ACCESS_KEY:minioadmin}
    secret-key: ${S3_SECRET_KEY:minioadmin123}
    bucket:
      media: media
      documents: documents
      uploads: uploads
```

### CLI Access

```bash
# Using mc (MinIO Client) inside container
docker exec -it kanokna-minio mc ls local/media
```

---

## Troubleshooting

### Service Won't Start

```bash
# Check service logs
docker compose logs <service-name>

# Check service health
docker compose ps

# Restart specific service
docker compose restart <service-name>
```

### Database Connection Issues

```bash
# Verify PostgreSQL is running
docker compose ps postgres

# Test connection
docker exec -it kanokna-postgres psql -U kanokna -d kanokna -c "SELECT 1;"

# Check init script execution
docker compose logs postgres | grep -i "init"
```

### Kafka Not Ready

```bash
# Check Zookeeper first
docker compose logs zookeeper

# Then check Kafka
docker compose logs kafka

# Verify broker is registered
docker exec -it kanokna-kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

### Elasticsearch Memory Issues

If Elasticsearch fails with memory errors:

```bash
# Increase vm.max_map_count (Linux/WSL2)
sudo sysctl -w vm.max_map_count=262144

# Make permanent
echo "vm.max_map_count=262144" | sudo tee -a /etc/sysctl.conf
```

### Reset Everything

```bash
# Stop and remove all containers, volumes, networks
docker compose down -v --remove-orphans

# Remove all Kanokna volumes
docker volume ls | grep kanokna | awk '{print $2}' | xargs docker volume rm

# Start fresh
docker compose up -d
```

---

## Directory Structure

```
deployment/
├── docker-compose.yml      # Main compose file
├── config/
│   └── prometheus.yml      # Prometheus configuration
├── env/
│   ├── .env               # Environment variables
│   ├── .env.example       # Template for .env
│   └── services.env       # Service-specific variables
├── init-db/
│   └── init-databases.sql # Database initialization
└── README.md              # This file
```

---

## Environment Variables

All configurable values are defined in `env/.env`. Key variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `POSTGRES_USER` | kanokna | Database username |
| `POSTGRES_PASSWORD` | kanokna_secret | Database password |
| `KAFKA_PORT` | 9092 | External Kafka port |
| `REDIS_PORT` | 6379 | Redis port |
| `ELASTICSEARCH_PORT` | 9200 | Elasticsearch port |
| `KEYCLOAK_PORT` | 8180 | Keycloak port |
| `MINIO_API_PORT` | 9000 | MinIO API port |

---

*Generated by GRACE-CODER for CWO-20251230-06-W0-T6*
