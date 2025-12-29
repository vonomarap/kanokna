#!/usr/bin/env bash
set -euo pipefail

ROLE="${1:-}"
SCOPE="${2:-}"
HANDOFF="${3:-}"

if [[ -z "$ROLE" || -z "$SCOPE" || -z "$HANDOFF" ]]; then
  echo "Usage: ./scripts/grace_commit.sh <role:architect|coder|coordinator> <scope> <handoffRef>"
  exit 1
fi

if [[ ! -f "docs/grace/handoffs/${HANDOFF}.xml" ]]; then
  echo "ERROR: missing handoff file docs/grace/handoffs/${HANDOFF}.xml"
  exit 1
fi

if [[ "$ROLE" == "coder" ]]; then
  if [[ ! -f "docs/grace/approvals.log" ]] || ! grep -q "ref=\"${HANDOFF}\".*status=\"APPROVED\"" docs/grace/approvals.log; then
    echo "ERROR: missing approval for ${HANDOFF} in docs/grace/approvals.log"
    exit 1
  fi
fi

if git diff --quiet && git diff --cached --quiet; then
  echo "Nothing to commit."
  exit 0
fi

BR="grace/${ROLE}/${HANDOFF}"
git rev-parse --verify "$BR" >/dev/null 2>&1 || git switch -c "$BR"
git switch "$BR"

if git diff --name-only | grep -q "^docs/grace/.*\.xml$"; then
  if grep -RIn " or " docs/grace/*.xml >/dev/null 2>&1; then
    echo "WARNING: found ' or ' in docs/grace/*.xml. Ensure No-OR rule is respected."
  fi
fi


git add -A

MSG="grace(${ROLE}): ${SCOPE} [handoff=${HANDOFF}]"
git commit -m "$MSG"

echo "Committed on branch: $BR"
