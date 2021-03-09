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

package org.omegat.gui.plugin;

import org.omegat.core.data.PluginInformation;
import org.omegat.gui.common.EntryInfoPane;
import org.omegat.util.OStrings;
import org.omegat.util.gui.DesktopWrapper;
import org.omegat.util.gui.StaticUIUtils;

import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLDocument;
import java.net.URI;

/**
 * Data pane to show plugin details in formatted text.
 *
 * @author Hiroshi Miura
 */
public class PluginDetailsPane extends EntryInfoPane<PluginInformation> {
    private static final long serialVersionUID = 7345812965508717972L;

    private static final String EXPLANATION = OStrings.getString("PREFS_PLUGINS_DETAILS_explanation");

    public PluginDetailsPane() {
        super(true);
        setContentType("text/html");
        ((HTMLDocument) getDocument()).setPreservesUnknownTags(false);
        setFont(getFont());
        setEditable(false);
        StaticUIUtils.makeCaretAlwaysVisible(this);
        setText(EXPLANATION);
        addHyperlinkListener(e -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                try {
                    DesktopWrapper.browse(URI.create(e.getURL().toString()));
                } catch (Exception ex) {
                    JOptionPane.showConfirmDialog(this, ex.getLocalizedMessage(),
                            OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
