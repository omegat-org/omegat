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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * Open HTTP(S) links in an external browser.
 *
 * @author Hiroshi Miura
 */
public class ExternalBrowserLaunchingLinkListener implements HyperlinkListener {

    @Override
    public void hyperlinkUpdate(final HyperlinkEvent hyperlinkEvent) {
        if (HyperlinkEvent.EventType.ACTIVATED.equals(hyperlinkEvent.getEventType())) {
            URL url = hyperlinkEvent.getURL();
            if ("https".equals(url.getProtocol()) || "http".equals(url.getProtocol())) {
                try {
                    DesktopWrapper.browse(url.toURI());
                } catch (IOException | URISyntaxException ignore) {
                }
            }
        }
    }
}
