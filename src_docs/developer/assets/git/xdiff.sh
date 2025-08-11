#!/bin/bash

# Create temporary files for the canonical XML content
LOCAL=$(mktemp)
REMOTE=$(mktemp)

# Perform the conversion to canonical XML format
xmllint --c14n11 "$1" > "$LOCAL"
xmllint --c14n11 "$2" > "$REMOTE"

# Run diff with canonical XML content
diff --unified=3 --ignore-case --color=always "$LOCAL" "$REMOTE" | less --raw-control-chars

# Clean up temporary files
rm -f "$LOCAL" "$REMOTE"

