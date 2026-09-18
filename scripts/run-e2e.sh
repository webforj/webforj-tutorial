#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
requested_step="${1:-all}"

if (( $# > 1 )) || [[ ! "$requested_step" =~ ^(all|[1-9][0-9]*)$ ]]; then
  printf 'Usage: bash scripts/run-e2e.sh [all|step-number]\n' >&2
  exit 2
fi

shopt -s nullglob
step_poms=("$repo_root"/[0-9]*-*/pom.xml)
selected_steps=()
failed_steps=()

for pom in "${step_poms[@]}"; do
  step_dir="$(dirname -- "$pom")"
  step="$(basename -- "$step_dir")"
  if [[ "$requested_step" != all && "$step" != "$requested_step"-* ]]; then
    continue
  fi

  selected_steps+=("$step")
  printf '\nRunning E2E tests in %s\n' "$step"
  if (cd -- "$step_dir" && mvn -B -ntp verify); then
    printf 'PASS: %s\n' "$step"
  else
    failed_steps+=("$step")
    printf 'FAIL: %s\n' "$step" >&2
  fi
done

if (( ${#selected_steps[@]} == 0 )); then
  printf 'No tutorial step found for: %s\n' "$requested_step" >&2
  exit 2
fi

if (( ${#failed_steps[@]} > 0 )); then
  printf '\nFailed tutorial steps:\n' >&2
  printf '  %s\n' "${failed_steps[@]}" >&2
  exit 1
fi

printf '\nAll %s selected tutorial steps passed.\n' "${#selected_steps[@]}"
