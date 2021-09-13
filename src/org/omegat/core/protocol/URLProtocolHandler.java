/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Hiroshi Miura
               Home page: http://www.omegat.org/
               Support center: https://omegat.org/support

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/
package org.omegat.core.protocol;

public final class URLProtocolHandler {
    // PKG should not have last `.data` for `data:` protocol.
    private static final String PKG =  "org.omegat.core.protocol";
    private static final String CONTENT_PATH_PROP = "java.protocol.handler.pkgs";

    private URLProtocolHandler() {
    }

    public static void install() {
        String handlerPkgs = System.getProperty(CONTENT_PATH_PROP, "");
        if (!handlerPkgs.contains(PKG)) {
            if (handlerPkgs.isEmpty()) {
                handlerPkgs = PKG;
            } else {
                handlerPkgs += "|" + PKG;
            }
            System.setProperty(CONTENT_PATH_PROP, handlerPkgs);
        }
    }
}
