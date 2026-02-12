.PHONY: help up up-tools up-monitoring down logs build test frontend-build frontend-test build-all test-all

help:
	@echo "Available targets:"
	@echo "  make help           Show all available targets"
	@echo "  make up             Start core local infrastructure"
	@echo "  make up-tools       Start infrastructure plus tools profile"
	@echo "  make up-monitoring  Start infrastructure plus monitoring profile"
	@echo "  make down           Stop local infrastructure"
	@echo "  make logs           Stream local infrastructure logs"
	@echo "  make build          Build backend (skip tests)"
	@echo "  make test           Run backend unit-focused tests"
	@echo "  make frontend-build Install and build frontend"
	@echo "  make frontend-test  Install and run frontend tests (non-watch)"
	@echo "  make build-all      Run backend build and frontend build"
	@echo "  make test-all       Run backend test and frontend test"

up:
	docker compose -f docker-compose.dev.yml up -d

up-tools:
	docker compose -f docker-compose.dev.yml --profile tools up -d

up-monitoring:
	docker compose -f docker-compose.dev.yml --profile monitoring up -d

down:
	docker compose -f docker-compose.dev.yml down

logs:
	docker compose -f docker-compose.dev.yml logs -f

build:
	cd backend/windows-store-server && mvn -B -DskipTests verify

test:
	cd backend/windows-store-server && mvn -B -DfailIfNoTests=false test

frontend-build:
	cd frontend/windows-store-client && npm ci --legacy-peer-deps && npm run build

frontend-test:
	cd frontend/windows-store-client && npm ci --legacy-peer-deps && npm run test -- --watch=false

build-all: build frontend-build

test-all: test frontend-test
