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
package org.omegat.util.gui;

import tokyo.northside.protocol.URLProtocolHandler;

/**
 * @author Hiroshi Miura
 */
public class HtmlMediaSupport {
    private HtmlMediaSupport() {
    }

    /**
     * load plugin.
     * <p>
     * load an URL protocol handler to allow "data:,base64:abc123..." inline data.
     */
    public static void loadPlugins() {
        URLProtocolHandler.install();
    }

    /**
     * unload plugin.
     */
    public static void unloadPlugins() {
    }
}
