# /**************************************************************************
#  OmegaT - Computer Assisted Translation (CAT) tool
#           with fuzzy matching, translation memory, keyword search,
#           glossaries, and translation leveraging into updated projects.
#
#  Copyright (C) 2022 Hiroshi Miura
#                Home page: http://www.omegat.org/
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
#  along with this program.  If not, see <http://www.gnu.org/licenses/>.
#  **************************************************************************/
#

FROM debian:bullseye-slim
RUN apt-get -y update && apt-get upgrade -y && apt-get install -y openssh-client git openjdk-11-jdk inotify-tools curl subversion
RUN adduser --disabled-password --gecos "" --home /home/omegat --shell /bin/bash omegat && mkdir -p /home/omegat/.ssh \
    && touch /home/omegat/.ssh/known_hosts && chmod 600 /home/omegat/.ssh/known_hosts
COPY ssh_config /home/omegat/.ssh/config
RUN chown -R omegat /home/omegat

USER omegat

CMD ([ -f /keys/id_rsa ] || inotifywait -e attrib /keys ) \
  && cp /keys/id_rsa /home/omegat/.ssh/id_rsa && chown omegat.omegat /home/omegat/.ssh/id_rsa && chmod 600 /home/omegat/.ssh/id_rsa \
  && git config --global user.name example && git config --global user.email git@example.com && git config --global http.sslVerify false \
  && [ "${TYPE}" = "SVN" ] && export REPO=http://server/svn/omegat-test.svn REPO2=http://server/svn/omegat-test.svn \
     || [ "${TYPE}" = "SSH" ] && export REPO=svn+ssh://git@server/home/git/omegat-test.svn REPO2=svn+ssh://git@server/home/git/omegat-test.svn \
     || [ "${TYPE}" = "GIT" ] && export REPO=git@server:omegat-test.git REPO2=https://git:gitpass@server/omegat-test.git \
     || export REPO=git@server:omegat-test.git REPO2=https://git:gitpass@server/omegat-test.git \
  && cd /code && ./gradlew testIntegration -Dsvnkit.ssh.client=apache -Domegat.test.duration=${DURATION} -Domegat.test.repo=${REPO} \
       -Domegat.test.repo.alt=${REPO2} -Domegat.test.map.repo=http://server/ -Domegat.test.map.file=README
