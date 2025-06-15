#!/bin/bash

# Get the last tag or fall back to HEAD^ if no tags exist
LAST_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "HEAD^")
if [ "$LAST_TAG" = "HEAD^" ]; then
    echo "Warning: No tags found, using previous commit as reference"
fi

# Get the current branch name
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
echo "Analyzing contributions since tag: $LAST_TAG on branch: $CURRENT_BRANCH"
echo

# Main code paths - remove trailing slashes
MAIN_PATHS=(
  "src"
  "aligner/src/main"
  "machinetranslators/apertium/src/main"
  "machinetranslators/belazar/src/main"
  "machinetranslators/dummy/src/main"
  "machinetranslators/google/src/main"
  "machinetranslators/ibmwatson/src/main"
  "machinetranslators/mymemory/src/main"
  "machinetranslators/yandex/src/main"
  "spellchecker/hunspell/src/main"
  "spellchecker/morfologik/src/main"
  "theme/src/main"
  "tipoftheday/src/main"
)

# Test code paths - remove trailing slashes
TEST_PATHS=(
  "test"
  "test-integration"
  "test-acceptance"
  "aligner/src/test"
  "machinetranslators/apertium/src/test"
  "machinetranslators/belazar/src/test"
  "machinetranslators/google/src/test"
  "machinetranslators/ibmwatson/src/test"
  "machinetranslators/mymemory/src/test"
  "machinetranslators/yandex/src/test"
  "spellchecker/hunspell/src/test"
  "spellchecker/morfologik/src/test"
)

DOCS_PATHS=(
  "doc_src"
  "src_docs"
  "release"
)

BUILD_PATHS=(
  "build-logic/src"
  "ci"
)

function join_paths() {
  local arr=("$@")
  echo "--" "${arr[@]}"
}

function analyze_contributions() {
  local paths=("$@")
  local range="${LAST_TAG}..${CURRENT_BRANCH}"

  # Get unique authors who modified files in the specified paths
  git log --use-mailmap --no-merges --format='%aN' "$range" -- "${paths[@]}" 2>/dev/null | sort -u | while read -r author; do
    [ -z "$author" ] && continue

    echo "--- $author ---"

    # Use -- to explicitly separate paths from options
    COMMITS=$(git log --use-mailmap --no-merges --author="$author" "$range" -- "${paths[@]}" --pretty=oneline 2>/dev/null | wc -l)

    # Store numstat output in a variable for debugging
    NUMSTAT_OUTPUT=$(git log --use-mailmap --no-merges --author="$author" "$range" --numstat -- "${paths[@]}" 2>/dev/null)

    # Number of files changed
    FILES=$(git log --use-mailmap --author="$author"  "$range" --name-only --pretty=format:  -- "${paths[@]}" 2>/dev/null | \
          sort | uniq | grep -v '^$' | wc -l)

    STATS=$(echo "$NUMSTAT_OUTPUT" | \
            awk 'NF==3 { if($1 != "-") add += $1; if($2 != "-") del += $2 }
                 END { printf "Lines added: %d, Lines removed: %d\n", add, del }')

    echo "Commits: $COMMITS"
    echo "$STATS"
    echo "Files changed: $FILES"
    echo
  done
}

# Main execution
echo "== Per-author contribution in MAIN code =="
analyze_contributions "${MAIN_PATHS[@]}"
echo
echo "== Per-author contribution in TEST code =="
analyze_contributions "${TEST_PATHS[@]}"
echo
echo "== Per-author contribution in BUILD code =="
analyze_contributions "${BUILD_PATHS[@]}"
echo
echo "== Per-author contribution in Documentation =="
analyze_contributions "${DOCS_PATHS[@]}"
