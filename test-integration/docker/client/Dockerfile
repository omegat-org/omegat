# /**************************************************************************
#  OmegaT - Computer Assisted Translation (CAT) tool
#           with fuzzy matching, translation memory, keyword search,
#           glossaries, and translation leveraging into updated projects.
#
#  Copyright (C) 2022-2024 Hiroshi Miura
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

FROM debian:bookworm-slim
ARG TARGETARCH
ARG CAVER=1.0.3-1
ARG JAVA=17
ARG JDKVER=jdk_17.0.14.0.0+7-1
ARG GRADLE=8.13
RUN apt-get -y update && apt-get upgrade -y && apt-get install -y openssh-client git inotify-tools curl subversion unzip rsync  \
 java-common libasound2 libfontconfig1 libfreetype6 libxi6 libxrender1 libxtst6 p11-kit locales
RUN adduser --disabled-password --gecos "" --home /home/omegat --shell /bin/bash omegat && mkdir -p /home/omegat/.ssh
RUN chown -R omegat.omegat /home/omegat
COPY --chown=omegat ssh_config /home/omegat/.ssh/config
COPY --chown=omegat gitconfig /home/omegat/.gitconfig
COPY entrypoint.sh /usr/local/bin/
RUN chmod 755 /usr/local/bin/entrypoint.sh && chmod 700 /home/omegat/.ssh && chmod 600 /home/omegat/.ssh/config
RUN echo TARGETARCH is ${TARGETARCH} && curl -LO https://packages.adoptium.net/artifactory/deb/pool/main/a/adoptium-ca-certificates/adoptium-ca-certificates_${CAVER}_all.deb \
    && curl -LO https://packages.adoptium.net/artifactory/deb/pool/main/t/temurin-${JAVA}/temurin-${JAVA}-${JDKVER}_${TARGETARCH}.deb \
    && dpkg -i adoptium-ca-certificates_${CAVER}_all.deb temurin-${JAVA}-${JDKVER}_${TARGETARCH}.deb \
    && rm -f adoptium-ca-certificates_${CAVER}_all.deb temurin-${JAVA}-${JDKVER}_${TARGETARCH}.deb
RUN (cd /opt && curl -LO https://services.gradle.org/distributions/gradle-${GRADLE}-bin.zip \
    && unzip -q gradle-${GRADLE}-bin.zip && rm -f gradle-${GRADLE}-bin.zip) && mv /opt/gradle-${GRADLE} /opt/gradle
RUN mkdir -p /gradle-cache && chown -R omegat:omegat /gradle-cache
RUN sed -i '/en_US.UTF-8/s/^# //g' /etc/locale.gen &&  locale-gen
RUN mkdir -p /workdir && chmod 1777 /workdir

USER omegat

# Configure locale
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8


ENTRYPOINT /usr/local/bin/entrypoint.sh
