# /**************************************************************************
#  OmegaT - Computer Assisted Translation (CAT) tool
#           with fuzzy matching, translation memory, keyword search,
#           glossaries, and translation leveraging into updated projects.
#
#  Copyright (C) 2022,2023 Hiroshi Miura
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
#  **************************************************************************/
#

FROM debian:bullseye-slim
RUN apt-get -y update && apt-get upgrade -y && apt-get install -y openssh-client git inotify-tools curl subversion unzip rsync  \
 java-common libasound2 libfontconfig1 libfreetype6 libxi6 libxrender1 libxtst6 p11-kit
RUN curl -LO https://packages.adoptium.net/artifactory/deb/pool/main/a/adoptium-ca-certificates/adoptium-ca-certificates_1.0.2-1_all.deb \
 && curl -LO https://packages.adoptium.net/artifactory/deb/pool/main/t/temurin-11/temurin-11-jdk_11.0.20.0.0+8_amd64.deb \
 && dpkg -i adoptium-ca-certificates_1.0.2-1_all.deb temurin-11-jdk_11.0.20.0.0+8_amd64.deb \
 && rm -f adoptium-ca-certificates_1.0.2-1_all.deb temurin-11-jdk_11.0.20.0.0+8_amd64.deb
RUN adduser --disabled-password --gecos "" --home /home/omegat --shell /bin/bash omegat && mkdir -p /home/omegat/.ssh
RUN chown -R omegat.omegat /home/omegat
COPY --chown=omegat ssh_config /home/omegat/.ssh/config
COPY --chown=omegat gitconfig /home/omegat/.gitconfig
COPY entrypoint.sh /usr/local/bin/
RUN (cd /opt && curl -LO https://services.gradle.org/distributions/gradle-7.5.1-bin.zip  \
    && unzip -q gradle-7.5.1-bin.zip && rm -f gradle-7.5.1-bin.zip)
RUN chmod 755 /usr/local/bin/entrypoint.sh && chmod 700 /home/omegat/.ssh && chmod 600 /home/omegat/.ssh/config

USER omegat

ENTRYPOINT /usr/local/bin/entrypoint.sh
