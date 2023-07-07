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

[ -f /keys/id_rsa ] || inotifywait -e attrib /keys

cp /keys/id_rsa /home/omegat/.ssh/id_rsa
chown omegat.omegat /home/omegat/.ssh/id_rsa
chmod 600 /home/omegat/.ssh/id_rsa

if [[ "${TYPE}" == "SVN" ]]; then
  export REPO=http://svn:svnpass@server/svn/omegat-test.svn
  export REPO2=svn+ssh://svn:svnpass@server/omegat-test.svn
elif [[ "${TYPE}" == "GIT" ]]; then
  export REPO=git@server:omegat-test.git
  export REPO2=https://git:gitpass@server/omegat-test.git
  git config --global user.name example
  git config --global user.email git@example.com
  git config --global http.sslVerify false
fi

ssh-keyscan -H server > /home/omegat/.ssh/known_hosts

cd /code
umask a+w
/opt/gradle-7.5.1/bin/gradle testIntegration -Domegat.test.duration=${DURATION} -Domegat.test.repo=${REPO} \
       -Domegat.test.repo.alt=${REPO2} -Domegat.test.map.repo=http://server/ -Domegat.test.map.file=README
result=$?
chmod -R a+w .gradle || true
find * -name build -type d -exec chmod -R a+w {} \; || true
exit $result
