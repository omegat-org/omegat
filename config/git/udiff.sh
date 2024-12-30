#!/bin/bash

# Unescape Unicode sequences in a properties file
unescape() {
    perl -CSD -Mopen=':std,:utf8' -pe 's/\\u([0-9a-fA-F]{4})/chr(hex($1))/eg' "$1"
}

# Create temporary files for the unescaped content
LOCAL=$(mktemp)
REMOTE=$(mktemp)

# Perform the unescaping
unescape "$1" > "$LOCAL"
unescape "$2" > "$REMOTE"

# Run diff with unescaped content
diff --unified=3 --ignore-case --color=always "$LOCAL" "$REMOTE" | less --raw-control-chars

# Clean up temporary files
rm -f "$LOCAL" "$REMOTE"

