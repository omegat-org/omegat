/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
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

package org.omegat.gui.editor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.undo.UndoableEdit;

import org.omegat.util.gui.UIThreadsUtil;

/**
 * Class for process undo/redo operations.
 *
 * We can't use standard UndoManager because OmegaT changes text attributes,
 * which affects on standard UndoManager. Instead, TranslationUndoManager
 * remember only text changes. But changed text should be stored only on
 * UndoableEditEvent, because composed text chars(Japanese, Chinese) should be
 * stored as one char.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class TranslationUndoManager implements UndoableEditListener {
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
        remember(0);
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
    public void remember(int caretPos) {
        UIThreadsUtil.mustBeSwingThread();

        synchronized (this) {
            Change ch = new Change();
            ch.text = editor.getOmDocument().extractTranslation();
            if (currentState != null) {
                if (ch.text.equals(currentState.text)) {
                    return;
                }
                currentState.caretPos = caretPos;
                undos.add(currentState);
            }
            currentState = ch;
            redos.clear();
        }
    }

    public void undoableEditHappened(UndoableEditEvent e) {
        UIThreadsUtil.mustBeSwingThread();

        if (inProgress || editor.getOmDocument().trustedChangesInProgress) {
            return;
        }
        AbstractDocument.DefaultDocumentEvent event = extractEvent(e.getEdit());
        if (event.getType() == DocumentEvent.EventType.CHANGE) {
            // attributes changed
            return;
        }

        int caretPos = event.getOffset() - editor.getOmDocument().getTranslationStart();
        if (event.getType() == DocumentEvent.EventType.REMOVE) {
            caretPos += event.getLength();
        }
        remember(caretPos);
    }

    private AbstractDocument.DefaultDocumentEvent extractEvent(UndoableEdit edit) {
        if (edit instanceof AbstractDocument.DefaultDocumentEvent) {
            // Java 8
            return (AbstractDocument.DefaultDocumentEvent) edit;
        } else if ("javax.swing.text.AbstractDocument.DefaultDocumentEventUndoableWrapper"
                .equals(edit.getClass().getCanonicalName())) {
            // Java 11
            try {
                return extractEventFromWrapper(edit);
            } catch (Exception e) {
                throw new RuntimeException("Failed to extract DefaultDocumentEvent from UndoableEdit", e);
            }
        } else {
            throw new RuntimeException("Failed to extract DefaultDocumentEvent from UndoableEdit; unknown class: "
                    + edit.getClass().getName());
        }
    }

    private AbstractDocument.DefaultDocumentEvent extractEventFromWrapper(UndoableEdit edit) throws Exception {
        Field ddeField = edit.getClass().getDeclaredField("dde");
        ddeField.setAccessible(true);
        return (AbstractDocument.DefaultDocumentEvent) ddeField.get(edit);
    }

    protected static final class Change {
        String text;
        int caretPos = -1;
    }
}
