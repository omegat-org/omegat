/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Zoltan Bartko
               2011 John Moran
               2015 Aaron Madlon-Kay
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

package org.omegat.gui.notes;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.undo.UndoManager;

import org.omegat.gui.common.EntryInfoPane;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.gui.main.IMainWindow;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.IPaneMenu;
import org.omegat.util.gui.JTextPaneLinkifier;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * This is a pane that displays notes on translation units.
 *
 * @author Martin Fleurke
 * @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
public class NotesTextArea extends EntryInfoPane<String> implements INotes, IPaneMenu {

    private static final String EXPLANATION = OStrings.getString("GUI_NOTESWINDOW_explanation");

    UndoManager undoManager;
    private DockableScrollPane scrollPane;

    /** Creates new Notes Text Area Pane */
    public NotesTextArea(IMainWindow mw) {
        super(true);

        String title = OStrings.getString("GUI_NOTESWINDOW_SUBWINDOWTITLE_Notes");
        scrollPane = new DockableScrollPane("NOTES", title, this, true);
        mw.addDockable(scrollPane);

        setEditable(false);
        setText(EXPLANATION);
        setMinimumSize(new Dimension(100, 50));

        JTextPaneLinkifier.linkify(this);
        undoManager = new UndoManager();
        getDocument().addUndoableEditListener(undoManager);
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
    @Override
    public void clear() {
        super.clear();
        setEditable(false);
        undoManager.discardAllEdits();
    }

    public void setNoteText(String text) {
        UIThreadsUtil.mustBeSwingThread();

        if (Preferences.isPreference(Preferences.NOTIFY_NOTES)) {
            if (StringUtil.isEmpty(text)) {
                scrollPane.stopNotifying();
            } else {
                scrollPane.notify(true);
            }
        }
        setText(text);
        setEditable(true);
    }

    public String getNoteText() {
        UIThreadsUtil.mustBeSwingThread();

        String text = getText();
        // Disallow empty note. Use null to indicate lack of note.
        return text.isEmpty() ? null : text;
    }

    @Override
    public void undo() {
        if (undoManager.canUndo()) {
            undoManager.undo();
        }
    }

    @Override
    public void redo() {
        if (undoManager.canRedo()) {
            undoManager.redo();
        }
    }

    @Override
    public void populatePaneMenu(JPopupMenu menu) {
        final JMenuItem notify = new JCheckBoxMenuItem(OStrings.getString("GUI_NOTESWINDOW_NOTIFICATIONS"));
        notify.setSelected(Preferences.isPreference(Preferences.NOTIFY_NOTES));
        notify.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Preferences.setPreference(Preferences.NOTIFY_NOTES, notify.isSelected());
            }
        });
        menu.add(notify);
    }
}
