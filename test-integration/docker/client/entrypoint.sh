#!/usr/bin/env bash
#
#  OmegaT - Computer Assisted Translation (CAT) tool
#           with fuzzy matching, translation memory, keyword search,
#           glossaries, and translation leveraging into updated projects.
#
#  Copyright (C) 2023 Hiroshi Miura.
#                Home page: https://www.omegat.org/
#                Support center: https://omegat.org/support
#
#  This file is part of OmegaT.
#
#  OmegaT is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  OmegaT is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see <https://www.gnu.org/licenses/>.
#
export GRADLE_USER_HOME=/gradle-cache

# Ensure UTF-8 encoding for all operations
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
export LANGUAGE=en_US:en

# Create hash of all build configuration files including version catalogs
BUILD_FILES_HASH=$(find /code/ -name "*.gradle*" -o -name "gradle.properties" -o -name "libs.versions.toml" \) \
    -not -path "./build/*" -not -path "./.gradle/*" -not -path "./.git/*" | \
    sort | xargs cat 2>/dev/null | sha256sum | cut -d' ' -f1)

rsync -rlD --exclude='.git' --exclude='.gradle' --exclude='build' /code/ /workdir

[ -f /keys/id_rsa ] || inotifywait -e attrib /keys

# Validate TYPE
if [[ -z "${TYPE}" ]]; then
  echo "TYPE is unset or empty. Defaulting to GIT."
  TYPE="GIT"
elif [[ "${TYPE}" != "SVN" && "${TYPE}" != "GIT" ]]; then
  echo "Invalid TYPE value '${TYPE}'. Expected 'SVN' or 'GIT'. Defaulting to GIT."
  TYPE="GIT"
fi

if [[ "${TYPE}" == "SVN" ]]; then
  export REPO=http://svn:svnpass@server/svn/omegat-test.svn
  export REPO2=svn+ssh://svn:svnpass@server/omegat-test.svn
else
  export REPO=git@server:omegat-test.git
  export REPO2=https://git:gitpass@server/omegat-test.git
fi

sleep 1
cat /dev/null > /home/omegat/.ssh/id_rsa
cat /keys/id_rsa > /home/omegat/.ssh/id_rsa
chmod 600 /home/omegat/.ssh/id_rsa

ssh-keyscan -H server > /home/omegat/.ssh/known_hosts

cd /workdir

CACHE_MARKER="${GRADLE_USER_HOME}/.deps-${BUILD_FILES_HASH}"

if [ ! -f "$CACHE_MARKER" ]; then
    echo "Build configuration changed or not cached. Downloading dependencies..."
    echo "Cache hash: ${BUILD_FILES_HASH:0:12}..."

    # First, resolve build-logic dependencies
    echo "Step 1/2: Resolving build-logic dependencies..."
    /opt/gradle/bin/gradle :build-logic:dependencies --no-daemon --quiet || {
        echo "Warning: Could not resolve build-logic dependencies, continuing..."
    }

    # Then resolve all project dependencies
    echo "Step 2/2: Resolving all project dependencies..."
    /opt/gradle/bin/gradle dependencies --no-daemon --parallel --continue || {
        echo "Warning: Some dependencies could not be resolved, continuing..."
    }

    # Mark as cached
    touch "$CACHE_MARKER"

    # Clean old cache markers (keep last 3 to handle different branches/configurations)
    find /gradle-cache -name ".deps-*" ! -name ".deps-${BUILD_FILES_HASH}" | \
        sort | head -n -3 | xargs rm -f 2>/dev/null || true

    echo "Dependencies cached successfully"
else
    echo "Dependencies already cached (hash: ${BUILD_FILES_HASH:0:12}...)"
fi
# Show cache statistics
CACHE_SIZE=$(du -sh /gradle-cache 2>/dev/null | cut -f1 || echo "unknown")
CACHED_CONFIGS=$(find /gradle-cache -name ".deps-*" | wc -l)
echo "Gradle cache size: $CACHE_SIZE, cached configurations: $CACHED_CONFIGS"

exec /opt/gradle/bin/gradle testIntegration --scan \
   -Djava.util.logging.config.file=/workdir/test-integration/logger.properties \
   -Domegat.test.duration=${DURATION} -Domegat.test.repo=${REPO} \
   -Domegat.test.repo.alt=${REPO2} -Domegat.test.map.repo=http://server/ -Domegat.test.map.file=README
