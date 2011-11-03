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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.gui.notes;

import java.awt.Dimension;

import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
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
public class NotesTextArea extends EntryInfoPane<Notes> {

    private static final long serialVersionUID = 1L;

    SourceTextEntry ste;
    
    String explanation;

    /** Creates new Notes Text Area Pane */
    public NotesTextArea(MainWindow mw) {
        super(true);

        String title = OStrings.getString("GUI_NOTESWINDOW_SUBWINDOWTITLE_Notes");
        mw.addDockable(new DockableScrollPane("NOTES", title, this, true));

        this.explanation =  OStrings.getString("GUI_NOTESWINDOW_explanation");

        setEditable(false);
        this.setText(this.explanation);
        setMinimumSize(new Dimension(100, 50));
    }

    @Override
    protected void startSearchThread(SourceTextEntry newEntry) {
        synchronized(this) {
            ste=newEntry;
        
            //We don't want to start a search thread, because noting is searched. 
            //The note is just fetched! 
            //Lets keep it simple.
            IProject project = Core.getProject();
            Notes notes = new Notes();
            TMXEntry tmxEntry = project.getTranslationInfo(newEntry);
            if (!tmxEntry.isTranslated()) {
                setFoundResult(newEntry, notes);
            } else {
                notes.tu = tmxEntry.note;
                setFoundResult(newEntry, notes);
            }
        }
    }

    /**
     * Sets the note to show in the pane. 
     */
    @Override
    protected void setFoundResult(final SourceTextEntry se, Notes notes) {
        UIThreadsUtil.mustBeSwingThread();
        synchronized (this) {
            if (notes == null) {
                this.setText("");
            } else {
                this.setText(notes.tu);
            }
            this.setEditable(true);
        }
    }

    @Override
    protected void onProjectOpen() {
        clear();
    }

    @Override
    protected void onProjectClose() {
        clear();
        this.setText(explanation);
    }


    /** Clears up the pane. */
    public void clear() {
        UIThreadsUtil.mustBeSwingThread();
        synchronized (this) {
            this.setText("");
            this.setEditable(false);
            this.ste = null;
        }
    }

    public String getText(SourceTextEntry ste) {
        synchronized (this) {
            if (this.ste != null && ste.equals(this.ste)) {
                return this.getText();
            } else return null;
        }
    }

}
