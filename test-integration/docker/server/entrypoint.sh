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

ssh-keygen -q -t rsa -m PEM -b 4096 -N '' -f /tmp/id_rsa && ssh-keygen -A
install -m 666 /tmp/id_rsa /tmp/id_rsa.pub /keys/
cat /keys/id_rsa.pub >> /home/git/.ssh/authorized_keys
rm -rf /var/run/apache2/* || true
echo "start servers"
exec /usr/bin/supervisord -c /root/supervisord.conf