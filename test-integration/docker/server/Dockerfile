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
ENV SUPERVISORDCONF=/etc/supervisor/conf.d/supervisord.conf
RUN apt-get -y update && apt-get install -y openssh-server git inotify-tools apache2 apache2-utils supervisor
RUN (adduser --system --shell /bin/bash git && mkdir -p /home/git/.ssh && chmod 700 /home/git/.ssh \
    && echo "AcceptEnv LANG LC_* GIT_PROTOCOL" >> /etc/ssh/sshd_config)
RUN (mkdir -p /home/git/template && git init /home/git/template && echo "source content" > /home/git/template/README \
    && cd /home/git/template && git config user.name git && git config user.email git@example.com \
    && git config init.defaultBranch main \
    && git add . && git commit -m first && git clone . /home/git/omegat-test.git --bare \
    && touch /home/git/.ssh/authorized_keys && chmod 600 /home/git/.ssh/authorized_keys \
    && chown git -R /home/git && mkdir -p /var/lock/apache2 /var/run/apache2 /var/run/sshd /var/log/supervisor )
RUN (mkdir -p /var/www/html && cp /home/git/template/README /var/www/html && chown -R www-data.www-data /var/www/html)
COPY supervisord.conf /etc/supervisor/conf.d/

EXPOSE 22 80 443
CMD ([ -f /keys/id_rsa.pub ] || inotifywait -e attrib /keys ) \
 && cat /keys/id_rsa.pub >> /home/git/.ssh/authorized_keys \
 && echo "start servers" && ssh-keygen -A && exec /usr/bin/supervisord
