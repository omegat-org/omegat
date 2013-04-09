/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.gui.common;

import java.awt.Font;

import javax.swing.JTextPane;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IFontChangedEventListener;
import org.omegat.core.events.IProjectEventListener;

/**
 * Base class for show information about currently selected entry. It can be used for glossaries, dictionaries
 * and other panes.
 * 
 * If you need long search operation, use EntryInfoThreadPane instead.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @param <T>
 *            result type of found data
 */
@SuppressWarnings("serial")
public abstract class EntryInfoPane<T> extends JTextPane implements IProjectEventListener {

    public EntryInfoPane(final boolean useApplicationFont) {
        if (useApplicationFont) {
            setFont(Core.getMainWindow().getApplicationFont());
            CoreEvents.registerFontChangedEventListener(new IFontChangedEventListener() {
                public void onFontChanged(Font newFont) {
                    EntryInfoPane.this.setFont(newFont);
                }
            });
        }
        CoreEvents.registerProjectChangeListener(this);
    }

    public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
        switch (eventType) {
        case CREATE:
        case LOAD:
            onProjectOpen();
            break;
        case CLOSE:
            onProjectClose();
            break;
        }
    }

    protected void onProjectOpen() {
    }

    protected void onProjectClose() {
    }
}
