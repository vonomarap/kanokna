
# Skill: coordinator-code-gates

## Purpose
Validate Coder outputs against scope, layering, contracts, logging, tests, and BranchSpec compliance.

## Trigger
Use when Coder returns:
- implementation code/tests/config/migrations
- GitExecutionSteps
- PR info

## Gates (PASS/FAIL)
### Gate 1: Scope & Determinism (BLOCKING)
- no new endpoints/events/services beyond approved handoff scope
- IDs preserved (UC/DP-SVC/MC/FC/BA/TC unchanged)
- no implementation for PENDING_HUMAN/TBD-dependent features

### Gate 2: Hexagonal/DDD Layering (BLOCKING)
- domain and application do NOT depend on adapters/bootstrap
- forbidden imports/annotations do not appear in domain/application
- @Transactional only in application services (or tx layer)
- Boundaries respected (domain free of Spring/JPA/etc.)
- controllers/listeners contain no business logic and call in-ports only

### Gate 3: Contracts & Observability (BLOCKING)
- contracts embedded verbatim where required
- logs follow canonical pattern and reference BA anchors
- correlation/trace fields handled as per contracts (or issue raised)

### Gate 4: Tests (BLOCKING for in-scope critical paths)
- TC-* cases implemented as tests
- failing/omitted tests explained only if explicitly allowed by scope (otherwise FAIL)

### Gate 5: Readability & Clean-Code (BLOCKING)
- no oversized methods/classes; minimal nesting
- consistent domain error strategy (no generic exceptions for domain failures)
- no mapper duplication (shared mapping utilities exist)
- magic numbers policy respected

### Gate 6: Git Compliance (BLOCKING)
- BranchSpec followed verbatim (branch name/base/PR target/merge method)
- no direct pushes to main/develop
- no force-push
- if release/hotfix to main: back-merge PR to develop exists (or explicit block)

If any fail produce a CodeIssueReport-YYYYMMDD-##.xml file in the docs/grace/reports/code-issue:

## FAIL Output → CodeIssueReport
```xml
<CodeIssueReport id="CODE-RPT-YYYYMMDD-##">
  <Issue id="ISS-...">
    <Severity>BLOCKING|NONBLOCKING</Severity>
    <Description>...</Description>
    <Evidence>File path / diff excerpt reference...</Evidence>
    <Impact>...</Impact>
    <ProposedFix>...</ProposedFix>
    <RequiredAgent>GRACE-CODER|GRACE-ARCHITECT</RequiredAgent>
  </Issue>
</CodeIssueReport>
```