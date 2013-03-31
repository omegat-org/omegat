/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
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

package org.omegat.gui.editor;

import java.util.ArrayList;
import java.util.List;

import org.omegat.util.gui.UIThreadsUtil;

/**
 * Class for process undo/redo operations.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class TranslationUndoManager {
    private final EditorTextArea3 editor;
    private final List<Change> undos = new ArrayList<Change>();
    private final List<Change> redos = new ArrayList<Change>();
    private Change currentState;
    private boolean inProgress;

    public TranslationUndoManager(EditorTextArea3 editor) {
        this.editor = editor;
    }

    public void reset() {
        UIThreadsUtil.mustBeSwingThread();

        synchronized (this) {
            undos.clear();
            redos.clear();
            currentState = null;
        }
    }

    public void undo() {
        UIThreadsUtil.mustBeSwingThread();

        Change ch;
        synchronized (this) {
            if (undos.isEmpty()) {
                return;
            }
            if (currentState != null) {
                redos.add(currentState);
            }
            ch = currentState = undos.remove(undos.size() - 1);
        }
        if (ch != null) {
            // apply
            apply(ch);
        }
    }

    public void redo() {
        UIThreadsUtil.mustBeSwingThread();

        Change ch;
        synchronized (this) {
            if (redos.isEmpty()) {
                return;
            }
            if (currentState != null) {
                undos.add(currentState);
            }
            ch = currentState = redos.remove(redos.size() - 1);
        }
        if (ch != null) {
            // apply
            apply(ch);
        }
    }

    /**
     * Apply change.
     */
    void apply(Change ch) {
        inProgress = true;
        try {
            editor.controller.replaceEditText(ch.text);
            if (ch.caretPos >= 0) {
                editor.setCaretPosition(editor.getOmDocument().getTranslationStart() + ch.caretPos);
            }
        } finally {
            inProgress = false;
        }
    }

    /**
     * Remember change.
     */
    public void changed() {
        if (inProgress) {
            return;
        }
        if (currentState != null) {
            currentState.caretPos = editor.getCaretPosition() - editor.getOmDocument().getTranslationStart();
        }
        Change ch = new Change();
        ch.text = editor.getOmDocument().extractTranslation();
        synchronized (this) {
            if (currentState != null) {
                undos.add(currentState);
            }
            currentState = ch;
            redos.clear();
        }
    }

    protected static final class Change {
        String text;
        int caretPos = -1;
    }
}
