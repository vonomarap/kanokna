# Kanokna Windows Store

Kanokna Windows Store is a microservices-based e-commerce platform for configurable windows and doors.

## Prerequisites

- Docker Desktop with Docker Compose v2
- Java 25
- Maven 3.9+
- Node.js and npm (required for frontend targets)
- GNU Make (`make`) command
  - Windows note: install `make` via Git Bash, MSYS2, Chocolatey, or WSL

## Quickstart

```bash
make up
make build
make test
make down
```

## Common Commands

| Command | Description |
| --- | --- |
| `make help` | Show all available targets |
| `make up` | Start core local infrastructure |
| `make up-tools` | Start infrastructure plus `tools` profile |
| `make up-monitoring` | Start infrastructure plus `monitoring` profile |
| `make down` | Stop local infrastructure |
| `make logs` | Stream Docker Compose logs |
| `make build` | Backend build (`mvn -B -DskipTests verify`) |
| `make test` | Backend unit-focused tests (`mvn -B -DfailIfNoTests=false test`) |
| `make build-all` | Run backend build, then frontend build |
| `make test-all` | Run backend tests, then frontend tests |

## Optional Frontend Commands

- `make frontend-build` - install dependencies and build Angular app
- `make frontend-test` - install dependencies and run Angular tests in non-watch mode

## More Documentation

- `backend/windows-store-server/deployment/README.md`
- `frontend/windows-store-client/README.md`
- `.github/README.md`

## Troubleshooting

- Missing `make` on Windows:
  - Install GNU Make via Git Bash, MSYS2, Chocolatey, or use WSL.
- Docker not running:
  - Start Docker Desktop and verify with `docker compose version`.
- `make test` scope:
  - Default `make test` is unit-focused. Full integration/contract coverage remains separate in CI or explicit manual commands.
