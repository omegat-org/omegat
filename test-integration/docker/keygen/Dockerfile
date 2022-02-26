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
RUN apt-get -y update && apt-get install -y openssh-client

CMD ssh-keygen -q -t rsa -m PEM -b 4096 -N '' -f /tmp/id_rsa \
  && install -m 666 /tmp/id_rsa /tmp/id_rsa.pub /keys/ && rm /tmp/id_rsa /tmp/id_rsa.pub \
  && tail -f /dev/null
