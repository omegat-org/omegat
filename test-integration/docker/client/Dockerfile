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
RUN apt-get -y update && apt-get upgrade -y && apt-get install -y openssh-client git openjdk-11-jdk inotify-tools curl
RUN adduser --disabled-password --gecos "" --home /home/omegat --shell /bin/bash omegat && mkdir -p /home/omegat/.ssh
COPY ssh_config /home/omegat/.ssh/config
RUN chmod 700 /home/omegat/.ssh && chown -R omegat /home/omegat

USER omegat

CMD ([ -f /keys/id_rsa ] || inotifywait -e attrib /keys ) \
  && cp /keys/id_rsa /home/omegat/.ssh/id_rsa \
  && chmod 600 /home/omegat/.ssh/id_rsa && chown omegat /home/omegat/.ssh/id_rsa \
  && git config --global user.name example && git config --global user.email ex@example.com \
  && git config --global http.sslVerify false \
  && echo "start test-integration" \
  && (cd /code \
    && ./gradlew testIntegration -Domegat.test.duration=${DURATION} -Domegat.test.repo=${REPO} -Domegat.test.repo1=${REPO1} -Domegat.test.maprepo=${MAPREPO} -Domegat.test.mapfile=${MAPFILE})
