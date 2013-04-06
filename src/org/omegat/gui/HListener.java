/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2013 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.gui;

import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.omegat.core.Core;
import org.omegat.gui.main.MainWindow;
import org.omegat.gui.tagvalidation.TagValidationFrame;

/**
 * A listener for old Hyperlink-like style
 * 
 * @author Keith Godfrey
 */
public class HListener implements HyperlinkListener {
    public HListener(MainWindow t, TagValidationFrame f, boolean grabFocus) {
        m_transFrame = t;
        m_tagValFrame = f;
        m_grabFocus = grabFocus;
    }

    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            final String desc = e.getDescription();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        if (desc.startsWith(TagValidationFrame.FIX_URL_PREFIX)) {
                            int entry = Integer.parseInt(desc.substring(TagValidationFrame.FIX_URL_PREFIX.length()));
                            String fixedSource = m_tagValFrame.fixEntry(entry);
                            Core.getEditor().gotoEntryAfterFix(entry, fixedSource);
                        } else {
                            Core.getEditor().gotoEntry(Integer.parseInt(desc));
                        }
                    } catch (NumberFormatException ex) {
                    }
                }
            });
            // m_transFrame.doGotoEntry(s);
            if (m_grabFocus) {
                m_transFrame.toFront();
            }
        }
    }

    private MainWindow m_transFrame;
    private boolean m_grabFocus;
    private TagValidationFrame m_tagValFrame;
}
