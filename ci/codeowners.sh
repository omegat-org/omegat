#!/usr/bin/env bash
#
# The script to use git-fame extension to generate CODEOWNERS
# See https://github.com/casperdcl/git-fame
#
owners(){
  for f in $(git ls-files); do
    # filename
    echo -n "$f "
    # author emails if loc distribution >= 30%
    git fame -esnwMC --incl "$f" | tr '/' '|' \
      | awk -F '|' '(NR>6 && $6>=30) {print $2}' \
      | xargs echo
  done
}

owners \
  | tqdm --total "$(git ls-files | wc -l)" \
    --unit file --desc "Generating CODEOWNERS" \
  > .github/CODEOWNERS