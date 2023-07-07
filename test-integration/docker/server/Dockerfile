# /**************************************************************************
#  OmegaT - Computer Assisted Translation (CAT) tool
#           with fuzzy matching, translation memory, keyword search,
#           glossaries, and translation leveraging into updated projects.
#
#  Copyright (C) 2022 Hiroshi Miura
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
RUN apt-get -y update && apt-get -y upgrade \
    && apt-get install -y openssh-server git inotify-tools apache2 apache2-utils supervisor apache2-suexec-pristine subversion libapache2-mod-svn \
    && adduser --system --group --shell /bin/bash git && (echo 'git:gitpass' | chpasswd) \
    && mkdir -p /home/git/.ssh && chmod 700 /home/git/.ssh \
    && adduser --system --group --shell /bin/bash svn && (echo 'svn:svnpass' | chpasswd) \
    && mkdir -p /home/svn/.ssh && chmod 700 /home/svn/.ssh \
    && a2dissite 000-default default-ssl && a2enmod ssl cgi env alias suexec dav dav_fs \
    && htpasswd -b -c /home/git/htpasswd git gitpass \
    && htpasswd -b -c /home/svn/htpasswd svn svnpass \
    && mkdir -p /var/lock/apache2 /var/run/apache2 /var/run/sshd /var/log/supervisor \
    && mkdir -p /var/www/git /var/www/html /home/svn/repos /home/svn/activities
COPY sshd_custom.conf /etc/ssh/sshd_config.d/
COPY http-*.conf /etc/apache2/sites-enabled/
COPY svn-access /home/svn/
COPY supervisord.conf /root/
COPY git-http-backend-wrapper.cgi /var/www/git/
COPY svnserve-wrapper.sh /home/svn/
COPY entrypoint.sh /usr/local/bin/
RUN chmod 755 /var/www/git/git-http-backend-wrapper.cgi \
    && chmod 755 /home/svn/svnserve-wrapper.sh \
    && chmod 755 /usr/local/bin/entrypoint.sh \
    && touch /home/git/.ssh/authorized_keys && chmod 600 /home/git/.ssh/authorized_keys \
    && mkdir -p /tmp/template && echo "source content" > /tmp/template/README \
    && (cd /home/svn/repos && svnadmin create omegat-test.svn  \
    && svn import /tmp/template file:///home/svn/repos/omegat-test.svn/trunk/ -m first ) \
    && echo "* -text" > /tmp/template/.gitattributes && git init /tmp/template \
    && (cd /tmp/template && git config user.name git && git config user.email git@example.com \
        && git config init.defaultBranch main && git add . && git commit -m first ) \
    && git clone /tmp/template /home/git/omegat-test.git --bare && (cd /home/git/omegat-test.git && git remote remove origin ) \
    && cp /tmp/template/README /var/www/html && chown -R www-data.www-data /var/www/html \
    && chown -R svn.svn /home/svn && chown -R www-data /home/svn/repos /home/svn/activities \
    && chmod -R a+w /home/svn/repos && chown -R git.git /home/git /var/www/git

EXPOSE 22 80 443
ENTRYPOINT /usr/local/bin/entrypoint.sh