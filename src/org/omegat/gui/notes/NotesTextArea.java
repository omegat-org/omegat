/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Zoltan Bartko
               2011 John Moran
               2015 Aaron Madlon-Kay
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

package org.omegat.gui.notes;

import java.awt.Dimension;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.common.EntryInfoPane;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.OStrings;
import org.omegat.util.gui.JTextPaneLinkifier;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * This is a pane that displays notes on translation units.
 * 
 * @author Martin Fleurke
 * @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
public class NotesTextArea extends EntryInfoPane<String> implements INotes {

    private static final String EXPLANATION = OStrings.getString("GUI_NOTESWINDOW_explanation");

    SourceTextEntry ste;

    /** Creates new Notes Text Area Pane */
    public NotesTextArea(MainWindow mw) {
        super(true);

        String title = OStrings.getString("GUI_NOTESWINDOW_SUBWINDOWTITLE_Notes");
        Core.getMainWindow().addDockable(new DockableScrollPane("NOTES", title, this, true));

        setEditable(false);
        setText(EXPLANATION);
        setMinimumSize(new Dimension(100, 50));
        
        JTextPaneLinkifier.linkify(this);
    }

    @Override
    protected void onProjectOpen() {
        clear();
    }

    @Override
    protected void onProjectClose() {
        clear();
        setText(EXPLANATION);
    }

    /** Clears up the pane. */
    public void clear() {
        UIThreadsUtil.mustBeSwingThread();

        setText("");
        setEditable(false);
        ste = null;
    }

    public void setNoteText(String text) {
        UIThreadsUtil.mustBeSwingThread();

        setText(text);
        setEditable(true);
    }

    public String getNoteText() {
        UIThreadsUtil.mustBeSwingThread();

        String text = getText();
        // Disallow empty note. Use null to indicate lack of note.
        return text.isEmpty() ? null : text;
    }
}
