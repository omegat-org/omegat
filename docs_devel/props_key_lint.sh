#!/bin/sh

# Check to see if all of the keys in the main strings file are actually used in
# the code. This is only approximate as some keys are computed dynamically.

# Known likely false positives
SKIP_KEYS=('HF_HAIKU_*'
           'STM_*'
           'SEGPROP_KEY_*')

function skip() {
    for K in ${SKIP_KEYS[@]}; do
        if [[ "$1" =~ $K ]]; then
            return 0
        fi
    done
    return 1
}           

function get_keys() {
    grep -v "^[# \t]" "$1" | grep "=" | cut -d "=" -f 1
}

function search() {
    return $(grep -qR --exclude '*.properties' "$1" src/org)
}

function lint() {
    for KEY in $(get_keys "$1"); do
        if ! skip "$KEY" && ! search "$KEY"; then
            echo "$KEY might not be used"
        fi
    done
}

LINT_FILE=src/org/omegat/Bundle.properties

if [ -f "$1" ] && [[ "$1" =~ *.properties ]]; then
    LINT_FILE="$1"
fi

if [ ! -f "$LINT_FILE" ]; then
    echo "Properties file not found. Run this from the repo root."
    exit 1
fi

lint "$LINT_FILE"
