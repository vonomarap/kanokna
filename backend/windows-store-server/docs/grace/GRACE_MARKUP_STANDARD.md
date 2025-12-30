# GRACE Markup Standard v2

**Version**: 2.0.0
**Effective Date**: 2025-12-30
**Schema Version**: `grace-markup-v2`

---

## 1. Overview

This document defines the canonical format for GRACE (Graph-RAG Anchored Code Engineering) markup tags used throughout the Windows & Doors E-Commerce platform. All agents (Architect, Coordinator, Coder) MUST use v2 markup format for handoffs and approvals.

### 1.1 Purpose

- **Deterministic Synthesis**: Enable Coder agents to implement from approved blueprints without ambiguity
- **RAG Navigation**: Support knowledge graph indexing with stable, predictable IDs
- **Audit Trail**: Maintain traceable approval chain with precise timestamps
- **Interoperability**: Ensure all agents parse the same format consistently

### 1.2 Schema Version Declaration

All v2 markup MUST include `schemaVersion="grace-markup-v2"` attribute.

---

## 2. Datetime Format (MANDATORY)

All datetime values MUST use **ISO-8601 with timezone offset**:

```
YYYY-MM-DDTHH:mm:ss±HH:MM
```

### Examples

| Valid | Invalid |
|-------|---------|
| `2025-12-30T14:35:00-08:00` | `2025-12-30` |
| `2025-12-30T22:35:00+00:00` | `2025-12-30 14:35:00` |
| `2025-12-30T09:00:00+03:00` | `12/30/2025 2:35 PM` |

### Timezone Guidelines

- Use the timezone where the action occurred
- For automated actions (agent-generated), use UTC (`+00:00`)
- For human approvals, use the approver's local timezone

---

## 3. GRACE_HANDOFF v2 Specification

### 3.1 Canonical Form

```xml
<GRACE_HANDOFF
  id="Handoff-YYYYMMDD-##"
  status="PROPOSED|APPROVED|SUPERSEDED|REJECTED"
  schemaVersion="grace-markup-v2"
  created="YYYY-MM-DDTHH:mm:ss±HH:MM"
  author="Human|AgentName"
  taskRef="W0-T#|W1-T#|..."
  planRef="DevelopmentExecutionPlan.xml#W0-T#"
  blueprintRef="DevelopmentPlan.xml#DP-SVC-..."
  techRef="Technology.xml#DEC-...,Technology.xml#DEC-..."
  requirementsRef="RequirementsAnalysis.xml#UC-..."
  supersedes="Handoff-YYYYMMDD-##"
>
  <Scope>
    <Services>
      <ServiceRef ref="DP-SVC-..."/>
    </Services>
    <UseCases>
      <UseCaseRef ref="UC-..."/>
    </UseCases>
  </Scope>

  <Artifacts>
    <Artifact ref="RequirementsAnalysis.xml" version="1.0"/>
    <Artifact ref="Technology.xml" version="1.0"/>
    <Artifact ref="DevelopmentPlan.xml" version="1.0"/>
  </Artifacts>

  <Contracts>
    <ModuleContractRef id="MC-..."/>
    <FunctionContractRef id="FC-..."/>
    <BlockAnchorRef id="BA-..."/>
  </Contracts>

  <ApprovalInstructions>
    <!-- Instructions for human/coordinator approval -->
  </ApprovalInstructions>
</GRACE_HANDOFF>
```

### 3.2 Attribute Definitions

| Attribute | Required | Description |
|-----------|----------|-------------|
| `id` | YES | Unique identifier: `Handoff-YYYYMMDD-##` |
| `status` | YES | One of: `PROPOSED`, `APPROVED`, `SUPERSEDED`, `REJECTED` |
| `schemaVersion` | YES | Must be `grace-markup-v2` |
| `created` | YES | ISO-8601 datetime when handoff was created |
| `author` | YES | Creator: `Human` or agent name (e.g., `GRACE-ARCHITECT`) |
| `taskRef` | YES | Task reference from DevelopmentExecutionPlan (e.g., `W0-T4`) |
| `planRef` | YES | Full reference to task in plan file |
| `blueprintRef` | YES | Service reference in DevelopmentPlan.xml |
| `techRef` | NO | Comma-separated technology decision references |
| `requirementsRef` | NO | Comma-separated requirement references |
| `supersedes` | CONDITIONAL | Previous handoff ID if this replaces another |

### 3.3 Status Rules

| Status | `approved` attr | `approver` attr | Description |
|--------|-----------------|-----------------|-------------|
| `PROPOSED` | OMIT | OMIT | Awaiting approval |
| `APPROVED` | REQUIRED | REQUIRED | Approved for implementation |
| `SUPERSEDED` | from original | from original | Replaced by newer handoff |
| `REJECTED` | OMIT | OMIT | Rejected with feedback |

### 3.4 Supersedes Rule

- Include `supersedes` ONLY when this handoff replaces a previous one
- Omit entirely for new handoffs (do not use `supersedes=""`)
- The superseded handoff's status should change to `SUPERSEDED`

---

## 4. GRACE_APPROVAL v2 Specification

### 4.1 Approvals.log-Only Policy (MANDATORY)

**All approvals MUST be recorded ONLY in `docs/grace/approvals.log`.**

- Handoff files (`docs/grace/handoffs/Handoff-*.xml`) MUST NOT contain any `<GRACE_APPROVAL .../>` tags
- The `approvals.log` file is the single authoritative source for approval status
- GRACE_HANDOFF `status` remains `PROPOSED`; the authoritative approval is the matching entry in `approvals.log`
- Agents MUST check `approvals.log` to determine approval status, not the handoff file itself

### 4.2 Canonical Form

```xml
<GRACE_APPROVAL
  ref="Handoff-YYYYMMDD-##"
  status="APPROVED"
  approved="YYYY-MM-DDTHH:mm:ss±HH:MM"
  approver="Human|AgentName"
/>
```

### 4.3 Attribute Definitions

| Attribute | Required | Description |
|-----------|----------|-------------|
| `ref` | YES | Handoff ID being approved (must match exactly) |
| `status` | YES | Must be `APPROVED` |
| `approved` | YES | ISO-8601 datetime of approval |
| `approver` | YES | Who approved: `Human` or coordinator name |

### 4.4 Optional Integrity Fields

For enhanced determinism, these optional fields may be included:

```xml
<GRACE_APPROVAL
  ref="Handoff-YYYYMMDD-##"
  status="APPROVED"
  approved="YYYY-MM-DDTHH:mm:ss±HH:MM"
  approver="Human"
  checksum="sha256:abc123..."
  basis="Human-Decision|Coordinator-PASS"
/>
```

| Attribute | Purpose |
|-----------|---------|
| `checksum` | SHA-256 hash of handoff content for tamper detection |
| `basis` | Approval reasoning (`Human-Decision` or `Coordinator-PASS`) |

### 4.5 Prohibition of Embedded Approvals

The following patterns are **FORBIDDEN** in handoff files:

```xml
<!-- FORBIDDEN: Do not embed GRACE_APPROVAL in handoff files -->
<ApprovalInstructions>
  <GRACE_APPROVAL ref="..." status="APPROVED" .../>
</ApprovalInstructions>
```

If a handoff file contains embedded `<GRACE_APPROVAL>` tags, agents MUST:
1. FAIL validation
2. Report the issue
3. Require removal before proceeding

---

## 5. ID Naming Conventions

### 5.1 Handoff IDs

Pattern: `Handoff-YYYYMMDD-##`

- `YYYYMMDD`: Date in ISO format
- `##`: Two-digit sequential number for that date

Examples:
- `Handoff-20251230-01` (first handoff on 2025-12-30)
- `Handoff-20251230-02` (second handoff on same date)

### 5.2 Reference ID Patterns

| Type | Pattern | Example |
|------|---------|---------|
| Task | `W#-T#` | `W0-T4` |
| Service | `DP-SVC-{name}` | `DP-SVC-gateway` |
| Use Case | `UC-{AREA}-{VERB}` | `UC-CATALOG-CONFIGURE-ITEM` |
| Module Contract | `MC-{svc}-{layer}-{type}` | `MC-gateway-infrastructure-GatewayApplication` |
| Function Contract | `FC-{svc}-{uc}-{method}` | `FC-gateway-filter-CorrelationIdFilter-filter` |
| Block Anchor | `BA-{SVC}-{AREA}-##` | `BA-GW-CORR-01` |
| Decision | `DEC-{AREA}-{NAME}` | `DEC-CONFIG-BACKEND` |

---

## 6. Migration from v1 to v2

### 6.1 Legacy Format Detection

v1 markup can be identified by:
- Missing `schemaVersion` attribute
- Date-only `created` value (e.g., `"2025-12-30"` instead of ISO-8601)
- Approval instructions suggesting legacy format

### 6.2 Transformation Rules

| v1 Field | v2 Field | Transformation |
|----------|----------|----------------|
| `created="2025-12-30"` | `created="2025-12-30T00:00:00+00:00"` | Append `T00:00:00+00:00` |
| (missing) | `schemaVersion="grace-markup-v2"` | Add attribute |
| (missing) | `taskRef`, `planRef`, `blueprintRef` | Extract from `<Scope>` content |
| `<Scope>` (nested XML) | `<Scope>` + attributes | Keep nested structure, add ref attributes |

### 6.3 Fallback Policy for Missing Timestamps

When historical timestamps are unavailable:

1. **Git commit time**: Use commit timestamp if file was committed
2. **File modification time**: Use filesystem mtime as fallback
3. **Default**: Use `YYYY-MM-DDT00:00:00+00:00` with note in `<MigrationNote>`

Example with migration note:
```xml
<GRACE_HANDOFF
  id="Handoff-20251229-01"
  ...
  created="2025-12-29T00:00:00+00:00"
>
  <MigrationNote>
    Timestamp approximated during v1→v2 migration (2025-12-30).
    Original file had date-only value.
  </MigrationNote>
```

---

## 7. No-Legacy-Markup Gate Rule

### 7.1 Coordinator Gate

The Coordinator agent MUST enforce the following gate before allowing implementation:

```xml
<GateRule id="GATE-NO-LEGACY-MARKUP">
  <Name>No Legacy Markup Gate</Name>
  <Description>
    Reject any handoff that does not conform to GRACE Markup v2.
  </Description>

  <Checks>
    <Check id="CHK-SCHEMA-VERSION">
      Verify schemaVersion="grace-markup-v2" is present
    </Check>
    <Check id="CHK-DATETIME-FORMAT">
      Verify all datetime attributes match ISO-8601 with offset
    </Check>
    <Check id="CHK-REQUIRED-ATTRS">
      Verify all required attributes are present
    </Check>
    <Check id="CHK-APPROVAL-FORMAT">
      If APPROVED, verify GRACE_APPROVAL v2 exists with matching ref
    </Check>
  </Checks>

  <OnFailure>
    <Action>BLOCK implementation</Action>
    <Action>Return BlueprintIssueReport with specific violations</Action>
    <Action>Request Architect to update handoff to v2 format</Action>
  </OnFailure>
</GateRule>
```

### 7.2 Validation Pseudocode

```
function validateHandoffV2(handoff):
  errors = []

  # Check schema version
  if handoff.schemaVersion != "grace-markup-v2":
    errors.append("Missing or invalid schemaVersion")

  # Check datetime format
  if not isISO8601WithOffset(handoff.created):
    errors.append("Invalid created datetime format")

  # Check required attributes
  for attr in ["id", "status", "author", "taskRef", "planRef", "blueprintRef"]:
    if not hasAttribute(handoff, attr):
      errors.append(f"Missing required attribute: {attr}")

  # Check approval if APPROVED
  if handoff.status == "APPROVED":
    approval = findApproval(handoff.id)
    if not approval:
      errors.append("APPROVED status but no GRACE_APPROVAL found")
    elif not isISO8601WithOffset(approval.approved):
      errors.append("Invalid approval datetime format")
    elif not approval.approver:
      errors.append("Missing approver in GRACE_APPROVAL")

  return errors
```

---

## 8. Examples

### 8.1 Complete PROPOSED Handoff (v2)

**Note:** Handoff files do NOT contain ApprovalInstructions or embedded GRACE_APPROVAL tags. Approvals are recorded only in `docs/grace/approvals.log`.

```xml
<GRACE_HANDOFF
  id="Handoff-20251230-07"
  status="PROPOSED"
  schemaVersion="grace-markup-v2"
  created="2025-12-30T15:30:00+03:00"
  author="GRACE-ARCHITECT"
  taskRef="W1-T1"
  planRef="DevelopmentExecutionPlan.xml#W1-T1"
  blueprintRef="DevelopmentPlan.xml#DP-SVC-catalog-configuration-service"
  techRef="Technology.xml#DEC-DB-ENGINE,Technology.xml#DEC-EVENT-SERIALIZATION"
  requirementsRef="RequirementsAnalysis.xml#UC-CATALOG-CONFIGURE-ITEM"
>
  <Scope>
    <Services>
      <ServiceRef ref="DP-SVC-catalog-configuration-service"/>
    </Services>
    <UseCases>
      <UseCaseRef ref="UC-CATALOG-CONFIGURE-ITEM"/>
      <UseCaseRef ref="UC-CATALOG-BROWSE"/>
    </UseCases>
  </Scope>

  <Artifacts>
    <Artifact ref="RequirementsAnalysis.xml" version="1.0.0"/>
    <Artifact ref="Technology.xml" version="1.0.0"/>
    <Artifact ref="DevelopmentPlan.xml" version="1.1.1"/>
  </Artifacts>

  <Contracts>
    <ModuleContractRef id="MC-catalog-configuration-service-domain-ConfigurationAggregate"/>
    <FunctionContractRef id="FC-catalog-configuration-service-validateConfiguration"/>
    <BlockAnchorRef id="BA-CFG-VAL-01"/>
    <BlockAnchorRef id="BA-CFG-VAL-02"/>
    <BlockAnchorRef id="BA-CFG-VAL-03"/>
  </Contracts>

  <!-- NO ApprovalInstructions section - approvals are in approvals.log only -->
</GRACE_HANDOFF>
```

### 8.2 Approvals in approvals.log (MANDATORY)

All approvals are recorded in `docs/grace/approvals.log`, NOT in handoff files:

```
# docs/grace/approvals.log

<GRACE_APPROVAL
  ref="Handoff-20251230-07"
  status="APPROVED"
  approved="2025-12-30T16:00:00+03:00"
  approver="Human"
/>
```

**Important:** The handoff file `status` attribute remains `PROPOSED`. The authoritative approval status is determined by checking `approvals.log` for a matching entry.

### 8.3 Superseding Handoff (v2)

```xml
<GRACE_HANDOFF
  id="Handoff-20251230-08"
  status="PROPOSED"
  schemaVersion="grace-markup-v2"
  created="2025-12-30T16:00:00+03:00"
  author="GRACE-ARCHITECT"
  taskRef="W0-T4"
  planRef="DevelopmentExecutionPlan.xml#W0-T4"
  blueprintRef="DevelopmentPlan.xml#DP-SVC-gateway"
  supersedes="Handoff-20251230-01"
>
  <!-- Updated handoff content -->
</GRACE_HANDOFF>
```

---

## 9. File Locations

| Content | Location | Notes |
|---------|----------|-------|
| Handoff documents | `docs/grace/handoffs/Handoff-*.xml` | MUST NOT contain `<GRACE_APPROVAL>` |
| **Approval log** | `docs/grace/approvals.log` | **MANDATORY** - single source of approval truth |
| This standard | `docs/grace/GRACE_MARKUP_STANDARD.md` | |
| Contract files | `docs/grace/contracts/*.xml` | |

### 9.1 Approval Log Format

The `approvals.log` file MUST:
- Use only `GRACE_APPROVAL v2` entries
- Include `approved` and `approver` attributes on every entry
- Use ISO-8601 datetime format with timezone offset
- Be append-only (new approvals added at the end)

Example `approvals.log` structure:
```
# GRACE Approvals Log
# Format: GRACE_APPROVAL v2 (approvals.log-only policy)

<GRACE_APPROVAL
  ref="Handoff-20251229-01"
  status="APPROVED"
  approved="2025-12-29T12:00:00+00:00"
  approver="Human"
/>

<GRACE_APPROVAL
  ref="Handoff-20251230-01"
  status="APPROVED"
  approved="2025-12-30T10:00:00+00:00"
  approver="Human"
/>
```

---

## 10. Changelog

| Version | Date | Changes |
|---------|------|---------|
| 2.0.0 | 2025-12-30 | Initial v2 specification with ISO-8601 datetime |
| 1.0.0 | 2025-12-29 | Legacy format (deprecated) |

---

## 11. References

- [ISO 8601 Date and Time Format](https://www.iso.org/iso-8601-date-and-time-format.html)
- GRACE Methodology: System prompt sections 20.1-20.5
- `DevelopmentExecutionPlan.xml` for task references
- `DevelopmentPlan.xml` for service references
