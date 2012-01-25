/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Zoltan Bartko
               2011 John Moran
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

package org.omegat.gui.notes;

import java.awt.Dimension;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.common.EntryInfoPane;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.OStrings;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * This is a pane that displays notes on translation units.
 * 
 * @author Martin Fleurke
 */
@SuppressWarnings("serial")
public class NotesTextArea extends EntryInfoPane<String> implements INotes {

    private static final String EXPLANATION = OStrings.getString("GUI_NOTESWINDOW_explanation");

    SourceTextEntry ste;

    /** Creates new Notes Text Area Pane */
    public NotesTextArea(MainWindow mw) {
        super(true);

        String title = OStrings.getString("GUI_NOTESWINDOW_SUBWINDOWTITLE_Notes");
        mw.addDockable(new DockableScrollPane("NOTES", title, this, true));

        this.setEditable(false);
        this.setText(EXPLANATION);
        this.setMinimumSize(new Dimension(100, 50));
    }

    @Override
    protected void onProjectOpen() {
        clear();
    }

    @Override
    protected void onProjectClose() {
        clear();
        this.setText(EXPLANATION);
    }

    /** Clears up the pane. */
    public void clear() {
        UIThreadsUtil.mustBeSwingThread();

        this.setText("");
        this.setEditable(false);
        this.ste = null;
    }

    public void setNoteText(String text) {
        UIThreadsUtil.mustBeSwingThread();

        this.setText(text != null ? text : "");
        this.setEditable(true);
    }

    public String getNoteText() {
        UIThreadsUtil.mustBeSwingThread();

        return this.getText();
    }
}
