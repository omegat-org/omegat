#!/bin/bash

# This script checks for duplicate dependencies in OmegaT source distribution.
# It checks for:
# 1. Same jar file in both lib/provided/core and lib/provided/modules
# 2. Same library but different version in both directories.

# Root of the source distribution
DIST_ROOT=$1

if [ -z "$DIST_ROOT" ]; then
    echo "Usage: $0 <dist_root>"
    exit 1
fi

CORE_DIR="$DIST_ROOT/lib/provided/core"
MODULES_DIR="$DIST_ROOT/lib/provided/modules"

echo "Checking for duplicates in:"
echo "  Core: $CORE_DIR"
echo "  Modules: $MODULES_DIR"

ERROR=0

# 1. Check for exact same jar file in both directories
echo "Checking for exact same jar files in both directories..."
if [ -d "$CORE_DIR" ] && [ -d "$MODULES_DIR" ]; then
    CORE_JARS=$(ls "$CORE_DIR"/*.jar 2>/dev/null | xargs -r -n 1 basename)
    for jar in $CORE_JARS; do
        if [ -f "$MODULES_DIR/$jar" ]; then
            echo "Error: Jar file '$jar' found in both core and modules directories."
            ERROR=1
        fi
    done
fi

# 2. Check for same library but different version
# We assume the jar naming convention is library-name-version.jar
# This logic is a bit heuristic but should catch common cases.
echo "Checking for different versions of the same library..."

check_multi_versions() {
    local search_dir=$1
    # Extract library name by removing version and .jar suffix
    # Typically version starts with a digit or follows a hyphen
    # Example: commons-lang3-3.12.0.jar -> commons-lang3
    # We use a regex to strip everything from the last hyphen followed by a digit
    # Special case: annotations-26.1.0.jar (JetBrains) and annotations-4.1.1.4.jar (Google Android)
    # are different libraries.
    local duplicates
    duplicates=$(find "$search_dir" -name "*.jar" 2>/dev/null | xargs -r -n 1 basename | \
        sed -E 's/^annotations-2*.jar$/google-annotations/; s/^annotations-4.*.jar$/jetbrains-annotations/' | \
        sed -E 's/(-[0-9].*)\.jar$//' | sort | uniq -d)
    
    if [ -n "$duplicates" ]; then
        echo "$duplicates" | while read lib; do
            echo "Error: Multiple versions of library '$lib' found in $search_dir:"
            ls "$search_dir/$lib"-[0-9]*.jar
        done
        ERROR=1
    fi
}

# Check within core
if [ -d "$CORE_DIR" ]; then
    check_multi_versions "$CORE_DIR"
fi

# Check within modules
if [ -d "$MODULES_DIR" ]; then
    check_multi_versions "$MODULES_DIR"
fi

if [ $ERROR -eq 0 ]; then
    echo "No duplicate dependencies found."
    exit 0
else
    echo "Duplicate dependencies found!"
    exit 1
fi
