/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Didier Briel
               2009-2010 Wildrich Fourie
               2010 Alex Buloichik
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

package org.omegat.gui.glossary;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.StringEntry;
import org.omegat.gui.common.EntryInfoPane;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * This is a Glossary pane that displays glossary entries.
 * 
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Wildrich Fourie
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class GlossaryTextArea extends EntryInfoPane<List<GlossaryEntry>> {
    /** Glossary manager instance. */
    protected final GlossaryManager manager = new GlossaryManager(this);

    /**
     * Currently processed entry. Used to detect if user moved into new entry.
     * In this case, new find should be started.
     */
    protected StringEntry processedEntry;

    /**
     * Holds the current GlossaryEntries for the TransTips
     */
    protected static List<GlossaryEntry> nowEntries;

    /** Creates new form MatchGlossaryPane */
    public GlossaryTextArea() {
        super(true);

        setEditable(false);

        String title = OStrings.getString("GUI_MATCHWINDOW_SUBWINDOWTITLE_Glossary");
        Core.getMainWindow().addDockable(new DockableScrollPane("GLOSSARY", title, this, true));

        addMouseListener(mouseListener);

        Core.getEditor().registerPopupMenuConstructors(200, new TransTipsPopup());
    }

    @Override
    protected void onProjectOpen() {
        clear();
        manager.start();
    }

    @Override
    protected void onProjectClose() {
        clear();
        manager.stop();
    }

    @Override
    protected void startSearchThread(SourceTextEntry newEntry) {
        new FindGlossaryThread(GlossaryTextArea.this, newEntry, manager).start();
    }

    /**
     * Refresh content on glossary file changed.
     */
    public void refresh() {
        SourceTextEntry ste = Core.getEditor().getCurrentEntry();
        if (ste != null) {
            startSearchThread(ste);
        }
    }

    /**
     * Sets the list of glossary entries to show in the pane. Each element of
     * the list should be an instance of {@link GlossaryEntry}.
     */
    protected void setFoundResult(SourceTextEntry en, List<GlossaryEntry> entries) {
        UIThreadsUtil.mustBeSwingThread();

        if (entries == null) {
            nowEntries = new ArrayList<GlossaryEntry>();
            setText("");
            return;
        }

        // If the TransTips is enabled then underline all the matched glossary
        // entries
        if (Preferences.isPreference(Preferences.TRANSTIPS)) {
            // TODO move marks construction into search thread
            highlightTransTips(en, entries);
        }

        nowEntries = entries;

        StringBuffer buf = new StringBuffer();
        for (GlossaryEntry entry : entries) {
            buf.append(entry.getSrcText() + " = " + entry.getLocText());
            if (entry.getCommentText().length() > 0)
                buf.append("\n" + entry.getCommentText());
            buf.append("\n\n");
        }
        setText(buf.toString());
    }

    /** Clears up the pane. */
    public void clear() {
        setText("");
    }

    /** {@inheritDoc} */
    public void highlightTransTips(SourceTextEntry en, List<GlossaryEntry> entries) {
        if (!entries.isEmpty()) {
            final List<Mark> marks = new ArrayList<Mark>();
            // Get the index of the current segment in the whole document
            String sourceText = en.getSrcText();
            sourceText = sourceText.toLowerCase();

            TransTips.Search callback = new TransTips.Search() {
                public void found(GlossaryEntry ge, int start, int end) {
                    marks.add(new Mark(Mark.ENTRY_PART.SOURCE, start, end));
                }
            };

            for (GlossaryEntry ent : entries) {
                TransTips.search(en.getSrcText(), ent, callback);
            }
            Core.getEditor().markActiveEntrySource(en, marks, TransTipsMarker.class.getName());
        }
    }

    /**
     * MouseListener for the GlossaryTextArea If there is text selected in the
     * Glossary it will be inserted in the Editor upon a right-click.
     */
    protected MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                insertTerm();
            }
        }
    };

    /**
     * Inserts the selected text into the EditorTextArea
     */
    private void insertTerm() {
        String selTxt = this.getSelectedText();
        if (selTxt == null) { /* Just do nothing */
        } else {
            Core.getEditor().insertText(selTxt);
        }
    }
}
