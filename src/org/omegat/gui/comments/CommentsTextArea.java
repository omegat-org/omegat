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
import org.omegat.core.data.SourceTextEntry;
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
public class CommentsTextArea extends EntryInfoPane<Comments> {

    private static final long serialVersionUID = 1L;

    String explanation;

    /** Creates new Comments Text Area Pane */
    public CommentsTextArea(MainWindow mw) {
        super(true);

        String title = OStrings.getString("GUI_COMMENTSWINDOW_SUBWINDOWTITLE_Comments");
        mw.addDockable(new DockableScrollPane("COMMENTS", title, this, true));

        this.explanation =  OStrings.getString("GUI_COMMENTSWINDOW_explanation");

        setEditable(false);
        this.setText(this.explanation);
        setMinimumSize(new Dimension(100, 50));
    }

    @Override
    protected void startSearchThread(SourceTextEntry newEntry) {
        //We don't want to start a search thread, because noting is searched. 
        //The comment is just fetched! 
        //Lets keep it simple.
        setFoundResult(newEntry, new Comments(newEntry.getComment()));
    }

    /**
     * Sets the comment to show in the pane. 
     */
    @Override
    protected void setFoundResult(final SourceTextEntry se, Comments comments) {
        UIThreadsUtil.mustBeSwingThread();
        this.setText(comments.comments);
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
        this.setText("");
    }

}
