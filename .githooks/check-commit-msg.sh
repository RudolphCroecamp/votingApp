#!/bin/sh
# Checks that a commit message is exactly one sentence on one line.
# Usage: check-commit-msg.sh <file-with-message>
# Used by the commit-messages job in CI.
msg=$(grep -v '^#' "$1" | sed -e 's/[[:space:]]*$//' | sed -e '/^$/d')

case "$msg" in
  Merge\ *|Revert\ *) exit 0 ;;   # messages git writes itself
esac

lines=$(printf '%s\n' "$msg" | grep -c .)
if [ "$lines" -ne 1 ]; then
  echo "Commit message must be one sentence on one line (found $lines lines):"
  printf '%s\n' "$msg" | sed 's/^/  /'
  exit 1
fi

# A second sentence = sentence-ending punctuation followed by more words.
if printf '%s' "$msg" | grep -Eq '[.!?][[:space:]]+[[:alnum:]]'; then
  echo "Commit message must be a single sentence:"
  echo "  $msg"
  exit 1
fi
exit 0
