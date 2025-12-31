# GitHub Configuration

This directory contains GitHub-specific configurations for the Kanokna Windows & Doors E-Commerce platform.

---

## MODULE_CONTRACT: MC-ci-github-actions

```
────────────────────────────────────────────────────────────────────────────────
ID:          MC-ci-github-actions
Type:        Infrastructure / CI-CD
Version:     1.0.0
Status:      IMPLEMENTED
────────────────────────────────────────────────────────────────────────────────

DESCRIPTION:
  Implements continuous integration and continuous deployment pipelines using
  GitHub Actions for the Kanokna microservices platform.

RESPONSIBILITIES:
  1. Build and compile all backend services on every push/PR
  2. Run unit, integration, architecture, and contract tests
  3. Perform code quality checks (linting, formatting)
  4. Build and push Docker images using Jib
  5. Deploy to staging environment
  6. Security scanning with CodeQL
  7. Automated dependency updates with Dependabot

QUALITY GATES:
  - All unit tests must pass
  - All integration tests must pass
  - No critical security vulnerabilities (CodeQL)
  - Code compiles without errors

LINKS:
  - DevelopmentPlan.xml#DP-INFRA-ci-cd
  - Technology.xml#TECH-CICD-001
  - RequirementsAnalysis.xml#NFR-CICD-001
────────────────────────────────────────────────────────────────────────────────
```

---

## Workflows

### 1. CI Workflow (`workflows/ci.yml`)

**Purpose:** Continuous Integration pipeline for every push and pull request.

| Job | Description | Trigger |
|-----|-------------|---------|
| `build` | Compile and verify the project | Push/PR to main, develop, feature/*, release/*, hotfix/* |
| `test` | Run unit, integration, ArchUnit, and contract tests | After build succeeds |
| `lint` | Check code style (Checkstyle, SpotBugs) | Parallel with build |
| `coverage` | Generate JaCoCo coverage report | After tests pass |

**Key Features:**
- Maven dependency caching
- Parallel job execution
- Test result artifacts (7-day retention)
- Coverage report artifacts

---

### 2. Build and Push (`workflows/build-and-push.yml`)

**Purpose:** Build Docker images using Jib and push to GitHub Container Registry.

| Trigger | Tag |
|---------|-----|
| Push to `main` | `latest` |
| Release tag `v*` | Version number |
| Manual dispatch | Custom tag |

**Services Built:**
- config-server
- gateway
- catalog-configuration-service
- pricing-service
- cart-service
- order-service
- media-service
- account-service
- notification-service
- reporting-service
- search-service
- installation-service
- product-service

**Image Registry:** `ghcr.io/{owner}/kanokna/{service}:{tag}`

---

### 3. Deploy to Staging (`workflows/deploy-stage.yml`)

**Purpose:** Deploy services to the staging Kubernetes cluster.

| Input | Description | Default |
|-------|-------------|---------|
| `services` | Services to deploy (comma-separated or "all") | `all` |
| `image_tag` | Docker image tag to deploy | `latest` |
| `dry_run` | Preview changes without applying | `false` |

**Prerequisites:**
- `KUBECONFIG_STAGE` secret (base64-encoded kubeconfig)
- Kubernetes cluster with namespace `kanokna-stage`

**Deployment Strategy:**
1. Helm charts (if available in `deploy/helm/{service}`)
2. Generated Kubernetes manifests (fallback)

---

### 4. CodeQL Security Analysis (`workflows/codeql.yml`)

**Purpose:** Security vulnerability scanning.

| Scan Type | Schedule | Description |
|-----------|----------|-------------|
| CodeQL | Push/PR + Weekly | SAST for Java code |
| Dependency Check | With CodeQL | OWASP vulnerability scan |

**Vulnerability Categories:**
- SQL Injection
- Cross-Site Scripting (XSS)
- Path Traversal
- Insecure Deserialization
- Known CVEs in dependencies

---

## Dependabot Configuration (`dependabot.yml`)

**Purpose:** Automated dependency updates.

| Ecosystem | Directory | Schedule | Grouping |
|-----------|-----------|----------|----------|
| Maven | `/backend/windows-store-server` | Weekly (Monday 06:00 UTC) | Spring Boot, Spring Cloud, Testing, Observability |
| GitHub Actions | `/` | Weekly | All actions |
| Docker | `/backend` | Weekly | Base images |
| npm | `/frontend` | Weekly | React, Testing, Build tools |

**PR Limits:**
- Maven: 10 open PRs
- Actions: 5 open PRs
- Docker: 3 open PRs
- npm: 10 open PRs

---

## Secrets Required

| Secret | Description | Used By |
|--------|-------------|---------|
| `GITHUB_TOKEN` | Automatic (GitHub-provided) | All workflows |
| `KUBECONFIG_STAGE` | Base64-encoded kubeconfig for staging | deploy-stage.yml |

---

## Branch Protection Rules (Recommended)

For `main` and `develop` branches:

- ✅ Require pull request reviews (1 approval)
- ✅ Require status checks to pass:
  - `build`
  - `test`
  - `lint`
  - `CodeQL`
- ✅ Require branches to be up to date
- ✅ Restrict pushes to administrators

---

## Workflow Status Badges

```markdown
![CI](https://github.com/{owner}/kanokna/actions/workflows/ci.yml/badge.svg)
![CodeQL](https://github.com/{owner}/kanokna/actions/workflows/codeql.yml/badge.svg)
![Deploy Stage](https://github.com/{owner}/kanokna/actions/workflows/deploy-stage.yml/badge.svg)
```

---

## Troubleshooting

### Build Failures

1. **Maven compilation errors:**
   - Check Java version (requires JDK 25)
   - Verify all modules are present in parent POM

2. **Test failures:**
   - Check test logs in artifacts
   - Spring Boot 4.x compatibility issues (TECH-ASSUM-001)

3. **Docker build failures:**
   - Verify Jib plugin is configured in service POM
   - Check GHCR authentication

### Deployment Failures

1. **Kubectl connection issues:**
   - Verify `KUBECONFIG_STAGE` secret is valid
   - Check cluster accessibility

2. **Helm failures:**
   - Verify chart exists at `deploy/helm/{service}`
   - Check values.yaml compatibility

---

## Contributing

When modifying workflows:

1. Test changes in a feature branch first
2. Use `workflow_dispatch` for manual testing
3. Check workflow syntax: `gh workflow view {name}`
4. Update this README if adding new workflows

---

*Generated by GRACE-CODER for CWO-20251230-05-W0-T5*
