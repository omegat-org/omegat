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

package org.omegat.gui.comments;

import java.awt.Dimension;

import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.gui.common.EntryInfoPane;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.OStrings;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * This is a pane that displays comments on source texts.
 * 
 * @author Martin Fleurke
 */
public class CommentsTextArea extends EntryInfoPane<Comments> implements IEntryEventListener {

    private static final String EXPLANATION = OStrings.getString("GUI_COMMENTSWINDOW_explanation");

    /** Creates new Comments Text Area Pane */
    public CommentsTextArea(MainWindow mw) {
        super(true);

        String title = OStrings.getString("GUI_COMMENTSWINDOW_SUBWINDOWTITLE_Comments");
        mw.addDockable(new DockableScrollPane("COMMENTS", title, this, true));

        setEditable(false);
        this.setText(EXPLANATION);
        setMinimumSize(new Dimension(100, 50));

        CoreEvents.registerEntryEventListener(this);
    }

    public void onEntryActivated(SourceTextEntry newEntry) {
        UIThreadsUtil.mustBeSwingThread();

        String comment = newEntry.getComment();
        this.setText(comment != null ? comment : "");
    }

    public void onNewFile(String activeFileName) {
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
    }
}
