/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2009 Didier Briel
               2010 Wildrich Fourie
               2013 Zoltan Bartko
               2014 Aaron Madlon-Kay
               2015 Yu Tang
               2023-2024 Thomas Cordonnier
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.editor;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.gui.editor.autocompleter.AutoCompleter;
import org.omegat.gui.shortcuts.PropertiesShortcuts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.Styles;
import org.omegat.util.gui.UIDesignManager;

/**
 * Changes of standard JEditorPane implementation for support custom behavior.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Wildrich Fourie
 * @author Zoltan Bartko
 */
@SuppressWarnings("serial")
public class EditorTextArea3 extends JEditorPane {

    private static final KeyStroke KEYSTROKE_CONTEXT_MENU = PropertiesShortcuts.getEditorShortcuts()
            .getKeyStroke("editorContextMenu");
    private static final KeyStroke KEYSTROKE_NEXT = PropertiesShortcuts.getEditorShortcuts()
            .getKeyStroke("editorNextSegment");
    private static final KeyStroke KEYSTROKE_PREV = PropertiesShortcuts.getEditorShortcuts()
            .getKeyStroke("editorPrevSegment");
    private static final KeyStroke KEYSTROKE_NEXT_NOT_TAB = PropertiesShortcuts.getEditorShortcuts()
            .getKeyStroke("editorNextSegmentNotTab");
    private static final KeyStroke KEYSTROKE_PREV_NOT_TAB = PropertiesShortcuts.getEditorShortcuts()
            .getKeyStroke("editorPrevSegmentNotTab");
    private static final KeyStroke KEYSTROKE_INSERT_LF = PropertiesShortcuts.getEditorShortcuts()
            .getKeyStroke("editorInsertLineBreak");
    private static final KeyStroke KEYSTROKE_SELECT_ALL = PropertiesShortcuts.getEditorShortcuts()
            .getKeyStroke("editorSelectAll");
    private static final KeyStroke KEYSTROKE_DELETE_PREV_TOKEN = PropertiesShortcuts.getEditorShortcuts()
            .getKeyStroke("editorDeletePrevToken");
    private static final KeyStroke KEYSTROKE_DELETE_NEXT_TOKEN = PropertiesShortcuts.getEditorShortcuts()
            .getKeyStroke("editorDeleteNextToken");
    private static final KeyStroke KEYSTROKE_FIRST_SEG = PropertiesShortcuts.getEditorShortcuts()
            .getKeyStroke("editorFirstSegment");
    private static final KeyStroke KEYSTROKE_LAST_SEG = PropertiesShortcuts.getEditorShortcuts()
            .getKeyStroke("editorLastSegment");
    private static final KeyStroke KEYSTROKE_SKIP_NEXT_TOKEN = PropertiesShortcuts.getEditorShortcuts()
            .getKeyStroke("editorSkipNextToken");
    private static final KeyStroke KEYSTROKE_SKIP_PREV_TOKEN = PropertiesShortcuts.getEditorShortcuts()
            .getKeyStroke("editorSkipPrevToken");
    private static final KeyStroke KEYSTROKE_SKIP_NEXT_TOKEN_SEL = PropertiesShortcuts.getEditorShortcuts()
            .getKeyStroke("editorSkipNextTokenWithSelection");
    private static final KeyStroke KEYSTROKE_SKIP_PREV_TOKEN_SEL = PropertiesShortcuts.getEditorShortcuts()
            .getKeyStroke("editorSkipPrevTokenWithSelection");
    private static final KeyStroke KEYSTROKE_TOGGLE_CURSOR_LOCK = PropertiesShortcuts.getEditorShortcuts()
            .getKeyStroke("editorToggleCursorLock");
    private static final KeyStroke KEYSTROKE_TOGGLE_OVERTYPE = PropertiesShortcuts.getEditorShortcuts()
            .getKeyStroke("editorToggleOvertype");

    /** Undo Manager to store edits */
    protected final TranslationUndoManager undoManager = new TranslationUndoManager(this);

    protected final EditorController controller;

    protected final List<PopupMenuConstructorInfo> popupConstructors = new ArrayList<PopupMenuConstructorInfo>();

    protected String currentWord;

    protected AutoCompleter autoCompleter = new AutoCompleter(this);

    /**
     * Whether or not we are confining the cursor to the editable part of the
     * text area. The user can optionally allow the caret to roam freely.
     *
     * @see #checkAndFixCaret(boolean)
     */
    protected boolean lockCursorToInputArea = true;

    /**
     * Flag indicating if the editor is in Insert (false) or Overwrite (true) mode.
     */
    protected boolean overtypeMode = false;

    public EditorTextArea3(EditorController controller) {
        this.controller = controller;
        setEditorKit(new StyledEditorKit() {
            public ViewFactory getViewFactory() {
                return FACTORY3;
            }

            protected void createInputAttributes(Element element, MutableAttributeSet set) {
                set.removeAttributes(set);
                EditorController c = EditorTextArea3.this.controller;
                try {
                    c.m_docSegList[c.displayedEntryIndex].createInputAttributes(element, set);
                } catch (Exception ex) {
                }
            }
        });

        addMouseListener(mouseListener);
        CoreEvents.registerEntryEventListener(lockListener);

        // Custom caret for overtype mode
        OvertypeCaret c = new OvertypeCaret();
        c.setBlinkRate(getCaret().getBlinkRate());
        setCaret(c);

        addCaretListener(e -> {
            try {
                int start = EditorUtils.getWordStart(EditorTextArea3.this, e.getMark());
                int end = EditorUtils.getWordEnd(EditorTextArea3.this, e.getMark());
                if (end - start <= 0) {
                    // word not defined
                    return;
                }
                String newWord = getText(start, end - start);
                if (!newWord.equals(currentWord)) {
                    currentWord = newWord;
                    CoreEvents.fireEditorNewWord(newWord);
                }
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        });
        setToolTipText("");
        setDragEnabled(true);
        setForeground(Styles.EditorColor.COLOR_FOREGROUND.getColor());
        setCaretColor(Styles.EditorColor.COLOR_FOREGROUND.getColor());
        setBackground(Styles.EditorColor.COLOR_BACKGROUND.getColor());

        updateLockInsertMessage();
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        Document3 doc = getOmDocument();
        if (doc != null) {
            doc.setFont(font);
        }
    }

    /**
     * Return OmDocument instead just a Document. If editor was not initialized
     * with OmDocument, it will contains other Document implementation. In this
     * case we don't need it.
     */
    public Document3 getOmDocument() {
        try {
            return (Document3) getDocument();
        } catch (ClassCastException ex) {
            return null;
        }
    }

    /**
     * Return true if the specified position is within the active translation
     * @param position
     * @return
     */
    public boolean isInActiveTranslation(int position) {
        return (position >= getOmDocument().getTranslationStart()
                && position <= getOmDocument().getTranslationEnd());
    }

    protected final transient MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            autoCompleter.setVisible(false);

            // Handle double-click
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                int mousepos = EditorTextArea3.this.viewToModel2D(e.getPoint());
                boolean changed = controller.goToSegmentAtLocation(getCaretPosition());
                if (!changed) {
                    if (selectTag(mousepos)) {
                        e.consume();
                    }
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                doPopup(e.getPoint());
            }
        };

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                doPopup(e.getPoint());
            }
        }

        private void doPopup(Point p) {
            int mousepos = EditorTextArea3.this.viewToModel2D(p);
            JPopupMenu popup = makePopupMenu(mousepos);
            if (popup.getComponentCount() > 0) {
                popup.show(EditorTextArea3.this, p.x, p.y);
            }
        }
    };

    private JPopupMenu makePopupMenu(int pos) {

        PopupMenuConstructorInfo[] cons;
        synchronized (popupConstructors) {
            /**
             * Copy constructors - for disable blocking in the procesing
             * time.
             */
            cons = popupConstructors.toArray(new PopupMenuConstructorInfo[popupConstructors.size()]);
        }

        boolean isInActiveEntry;
        int ae = controller.displayedEntryIndex;
        SegmentBuilder sb = controller.m_docSegList[ae];
        if (sb.isActive()) {
            isInActiveEntry = pos >= sb.getStartPosition() && pos <= sb.getEndPosition();
        } else {
            isInActiveEntry = false;
        }

        JPopupMenu popup = new JPopupMenu();
        for (PopupMenuConstructorInfo c : cons) {
            // call each constructor
            c.constructor.addItems(popup, EditorTextArea3.this, pos, isInActiveEntry,
                    isInActiveTranslation(pos), sb);
        }

        UIDesignManager.removeUnusedMenuSeparators(popup);

        return popup;
    }

    /**
     * Add new constructor into list and sort full list by priority.
     */
    protected void registerPopupMenuConstructors(int priority, IPopupMenuConstructor constructor) {
        synchronized (popupConstructors) {
            popupConstructors.add(new PopupMenuConstructorInfo(priority, constructor));
            Collections.sort(popupConstructors, (o1, o2) -> o1.priority - o2.priority);
        }
    }
        
    /** 
     * On new entry, check if we are in a locked segment, in which case we lock the editor
     * Using this method ensures that isLocked is set here and only here
     **/
    protected final class LockListener implements IEntryEventListener {
        private String isLocked = null;
        
        public void onEntryActivated(SourceTextEntry newEntry) {
            isLocked = null;
            SourceTextEntry entry = controller.getCurrentEntry();
            String[] props = entry.getRawProperties();
            for (int i = 0; i < props.length; i++) {
                if (props[i].equals("LOCKED")) {
                    isLocked = props[i + 1];
                    // Entries populated via a filter are always with context, as alternative
                    controller.setAlternateTranslationForCurrentEntry(true);
                }
            }
            if (isLocked == null) {
                TMXEntry tmx = Core.getProject().getTranslationInfo(entry);
                isLocked =  (tmx.linked == TMXEntry.ExternalLinked.xENFORCED) ? "tm/enforce" : null;
            }
        }
        
        public void onNewFile(String activeFileName) {
        }
    };
    private final transient LockListener lockListener = new LockListener();
    
    public void unlockSegment() {
        lockListener.isLocked = null;
    }    

    /**
     * Redefine some keys behavior. We can't use key listeners, because we have
     * to make something AFTER standard keys processing.
     */
    @Override
    protected void processKeyEvent(KeyEvent e) {
        int keyEvent = e.getID();
        if (keyEvent == KeyEvent.KEY_RELEASED) {
            // key released
            super.processKeyEvent(e);
            return;
        } else if (keyEvent == KeyEvent.KEY_TYPED) {
            //key typed
            super.processKeyEvent(e);
            return;
        }

        boolean processed = false;

        Document3 doc = getOmDocument();

        KeyStroke s = KeyStroke.getKeyStrokeForEvent(e);

        // non-standard processing
        if (autoCompleter.processKeys(e)) {
            // The AutoCompleter needs special treatment.
            processed = true;
        } else if (s.equals(KEYSTROKE_CONTEXT_MENU)) {
            // Context Menu key for contextual (right-click) menu (Shift+Esc on Mac)
            JPopupMenu popup = makePopupMenu(getCaretPosition());
            if (popup.getComponentCount() > 0) {
                popup.show(EditorTextArea3.this,
                        (int) getCaret().getMagicCaretPosition().getX(),
                        (int) getCaret().getMagicCaretPosition().getY());
                processed = true;
            }
        } else if (s.equals(KEYSTROKE_NEXT)) {
            // Advance when 'Use TAB to advance'
            if (controller.settings.isUseTabForAdvance()) {
                controller.nextEntry();
                processed = true;
            } else if (lockListener.isLocked != null) {
                // We should not accept any character, including TAB
                processed = Preferences.isPreferenceDefault(Preferences.SUPPORT_LOCKED_SEGMENTS, true);
            }
        } else if (s.equals(KEYSTROKE_PREV)) {
            // Go back when 'Use TAB to advance'
            if (controller.settings.isUseTabForAdvance()) {
                controller.prevEntry();
                processed = true;
            }
        } else if (s.equals(KEYSTROKE_NEXT_NOT_TAB)) {
            // Advance when not 'Use TAB to advance'
            if (!controller.settings.isUseTabForAdvance()) {
                controller.nextEntry();
                processed = true;
            } else {
                Core.getMainWindow().showTimedStatusMessageRB("ETA_WARNING_TAB_ADVANCE");
                processed = true;
            }
        } else if (s.equals(KEYSTROKE_PREV_NOT_TAB)) {
            // Go back when not 'Use TAB to advance'
            if (!controller.settings.isUseTabForAdvance()) {
                controller.prevEntry();
                processed = true;
            }
        } else if (s.equals(KEYSTROKE_INSERT_LF)) {
            // Insert LF
            KeyEvent ke = new KeyEvent(e.getComponent(), e.getID(), e.getWhen(), 0, KeyEvent.VK_ENTER, '\n');
            super.processKeyEvent(ke);
            processed = true;
        } else if (s.equals(KEYSTROKE_SELECT_ALL)) {
            // Select all
            setSelectionStart(doc.getTranslationStart());
            setSelectionEnd(doc.getTranslationEnd());
            processed = true;
        } else if (s.equals(KEYSTROKE_DELETE_PREV_TOKEN)) {
            // Delete previous token
            try {
                processed = wholeTagDelete(false);
                if (!processed) {
                    int offset = getCaretPosition();
                    int prevWord = Utilities.getPreviousWord(this, offset);
                    int c = Math.max(prevWord, doc.getTranslationStart());
                    setSelectionStart(c);
                    setSelectionEnd(offset);
                    replaceSelection("");

                    processed = true;
                }
            } catch (BadLocationException ex) {
                // do nothing
            }
        } else if (s.equals(KEYSTROKE_DELETE_NEXT_TOKEN)) {
            // Delete next token
            try {
                processed = wholeTagDelete(true);
                if (!processed) {
                    int offset = getCaretPosition();
                    int nextWord = Utilities.getNextWord(this, offset);
                    int c = Math.min(nextWord, doc.getTranslationEnd());
                    setSelectionStart(offset);
                    setSelectionEnd(c);
                    replaceSelection("");

                    processed = true;
                }
            } catch (BadLocationException ex) {
                // do nothing
            }
        } else if (s.equals(KEYSTROKE_FIRST_SEG)) {
            // Jump to beginning of document
            int segNum = controller.m_docSegList[0].segmentNumberInProject;
            controller.gotoEntry(segNum);
            processed = true;
        } else if (s.equals(KEYSTROKE_LAST_SEG)) {
            // Jump to end of document
            int lastSegIndex = controller.m_docSegList.length - 1;
            int segNum = controller.m_docSegList[lastSegIndex].segmentNumberInProject;
            controller.gotoEntry(segNum);
            processed = true;
        } else if (s.equals(KEYSTROKE_SKIP_PREV_TOKEN)) {
            // Skip over previous token
            processed = moveCursorOverTag(false, false);
        } else if (s.equals(KEYSTROKE_SKIP_PREV_TOKEN_SEL)) {
            // Skip over previous token while extending selection
            processed = moveCursorOverTag(true, false);
        } else if (s.equals(KEYSTROKE_SKIP_NEXT_TOKEN)) {
            // Skip over next token
            processed = moveCursorOverTag(false, true);
        } else if (s.equals(KEYSTROKE_SKIP_NEXT_TOKEN_SEL)) {
            // Skip over next token while extending selection
            processed = moveCursorOverTag(true, true);
        } else if (s.equals(KEYSTROKE_TOGGLE_CURSOR_LOCK)) {
            boolean lockEnabled = !lockCursorToInputArea;
            lockCursorToInputArea = lockEnabled;
            updateLockInsertMessage();
        } else if (s.equals(KEYSTROKE_TOGGLE_OVERTYPE)) {
            processed = switchOvertypeMode();
            updateLockInsertMessage();
        }

        // leave standard processing if need
        if (processed) {
            e.consume();
        } else {
            if ((e.getModifiersEx()
                    & (KeyEvent.CTRL_DOWN_MASK | KeyEvent.META_DOWN_MASK | KeyEvent.ALT_DOWN_MASK)) == 0) {
                // there is no Alt,Ctrl,Cmd keys, i.e. it's char
                if (e.getKeyCode() != KeyEvent.VK_SHIFT && !isNavigationKey(e.getKeyCode())) {
                    // it's not a single 'shift' press or navigation key
                    // fix caret position prior to inserting character
                    checkAndFixCaret(true);
                }
            }
            // Treat the case of enforced translations which should be locked - this case does not seem to be treated via replaceSelection        
            if (lockListener.isLocked != null) {
                if ((e.getKeyCode() == KeyEvent.VK_BACK_SPACE) || (e.getKeyCode() == KeyEvent.VK_DELETE)) {
                    Core.getMainWindow().showStatusMessageRB("MW_SEGMENT_LOCKED", lockListener.isLocked);
                    if (Preferences.isPreferenceDefault(Preferences.SUPPORT_LOCKED_SEGMENTS, true)) {
                        return;
                    }
                }
            }
            super.processKeyEvent(e);
            // note that the translation start/end position are not updated yet. This has been updated when
            // then keyreleased event occurs.
        }

        // some after-processing catches
        if (!processed && e.getKeyChar() != 0 && isNavigationKey(e.getKeyCode())) {
            // if caret is moved over existing chars, check and fix caret position
            // works only in after-processing if translation length (start and end position) has not changed,
            // because start and end position are not updated yet.
            checkAndFixCaret(false);
            autoCompleter.updatePopup(true);
        }
    }

    private void updateLockInsertMessage() {
        String lock = OStrings.getString("MW_STATUS_CURSOR_LOCK_" + (lockCursorToInputArea ? "ON" : "OFF"));
        String ins = OStrings.getString("MW_STATUS_CURSOR_OVERTYPE_" + (overtypeMode ? "ON" : "OFF"));

        String lockTip = OStrings.getString("MW_STATUS_TIP_CURSOR_LOCK_" + (lockCursorToInputArea ? "ON" : "OFF"));
        String insTip = OStrings.getString("MW_STATUS_TIP_CURSOR_OVERTYPE_" + (overtypeMode ? "ON" : "OFF"));
        Core.getMainWindow().showLockInsertMessage(lock + " | " + ins, lockTip + " | " + insTip);
    }

    private boolean switchOvertypeMode() {
        boolean switchOvertypeMode = !overtypeMode;
        overtypeMode = switchOvertypeMode;

        if (overtypeMode) {
            // Change the caret shape, width and color
            setCaretColor(Styles.EditorColor.COLOR_BACKGROUND.getColor());
            putClientProperty("caretWidth", getCaretWidth());

            // We need to force the caret damage to have the rectangle to correctly show up,
            // otherwise half of the caret is shown.
            try {
                OvertypeCaret caret = (OvertypeCaret) getCaret();
                Rectangle r = modelToView2D(caret.getDot()).getBounds();
                caret.damage(r);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        } else {
            // reset to default insert caret
            setCaretColor(Styles.EditorColor.COLOR_FOREGROUND.getColor());
            putClientProperty("caretWidth", 1);
        }
        return true;
    }

    private boolean isNavigationKey(int keycode) {
        switch (keycode) {
        // if caret is moved over existing chars, check and fix caret position
        case KeyEvent.VK_HOME:
        case KeyEvent.VK_END:
        case KeyEvent.VK_LEFT:
        case KeyEvent.VK_RIGHT:
        case KeyEvent.VK_UP:
        case KeyEvent.VK_DOWN:
        case KeyEvent.VK_KP_LEFT:
        case KeyEvent.VK_KP_RIGHT:
        case KeyEvent.VK_KP_UP:
        case KeyEvent.VK_KP_DOWN:
            return true;
        }
        return false;
    }

    /**
     * Move cursor over tag(possible, with selection)
     *
     * @param withShift
     *            true if selection need
     * @param checkTagStart
     *            true if check tag start, false if check tag end
     * @return true if tag processed
     */
    boolean moveCursorOverTag(boolean withShift, boolean checkTagStart) {
        Document3 doc = getOmDocument();
        int caret = getCaretPosition();
        int start = doc.getTranslationStart();
        int end = doc.getTranslationEnd();
        if (caret < start || caret > end) {
            // We are outside the translation (maybe cursor lock is off).
            // Don't try to jump over tags.
            return false;
        }
        if ((caret == start && !checkTagStart) || (caret == end && checkTagStart)) {
            // We are at the edge of the translation but moving toward the outside.
            // Don't try to jump over tags.
            return false;
        }
        SourceTextEntry ste = doc.controller.getCurrentEntry();
        String text = doc.extractTranslation();
        int off = caret - start;
        // iterate by 'protected parts'
        if (ste != null) {
            for (ProtectedPart pp : ste.getProtectedParts()) {
                if (checkTagStart) {
                    if (StringUtil.isSubstringAfter(text, off, pp.getTextInSourceSegment())) {
                        int pos = off + start + pp.getTextInSourceSegment().length();
                        if (withShift) {
                            getCaret().moveDot(pos);
                        } else {
                            getCaret().setDot(pos);
                        }
                        return true;
                    }
                } else {
                    if (StringUtil.isSubstringBefore(text, off, pp.getTextInSourceSegment())) {
                        int pos = off + start - pp.getTextInSourceSegment().length();
                        if (withShift) {
                            getCaret().moveDot(pos);
                        } else {
                            getCaret().setDot(pos);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Whole tag delete before or after cursor
     *
     * @param checkTagStart
     *            true if check tag start, false if check tag end
     * @return true if tag deleted
     */
    boolean wholeTagDelete(boolean checkTagStart) throws BadLocationException {
        Document3 doc = getOmDocument();
        SourceTextEntry ste = doc.controller.getCurrentEntry();
        String text = doc.extractTranslation();
        int off = getCaretPosition() - doc.getTranslationStart();
        // iterate by 'protected parts'
        if (ste != null) {
            for (ProtectedPart pp : ste.getProtectedParts()) {
                if (checkTagStart) {
                    if (StringUtil.isSubstringAfter(text, off, pp.getTextInSourceSegment())) {
                        int pos = off + doc.getTranslationStart();
                        doc.remove(pos, pp.getTextInSourceSegment().length());
                        return true;
                    }
                } else {
                    if (StringUtil.isSubstringBefore(text, off, pp.getTextInSourceSegment())) {
                        int pos = off + doc.getTranslationStart() - pp.getTextInSourceSegment().length();
                        doc.remove(pos, pp.getTextInSourceSegment().length());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Try to select full tag on specified position, in the source and
     * translation part of segment.
     *
     * @param pos
     *            position
     * @return true if selected
     */
    boolean selectTag(int pos) {
        int s = controller.getSegmentIndexAtLocation(pos);
        if (s < 0) {
            return false;
        }
        SegmentBuilder segment = controller.m_docSegList[s];
        if (pos < segment.getStartPosition() || pos >= segment.getEndPosition()) {
            return false;
        }
        SourceTextEntry ste = getOmDocument().controller.getCurrentEntry();
        if (ste != null) {
            try {
                String text = getOmDocument().getText(segment.getStartPosition(),
                        segment.getEndPosition() - segment.getStartPosition());
                int off = pos - segment.getStartPosition();
                if (off < 0 || off >= text.length()) {
                    return false;
                }
                for (ProtectedPart pp : ste.getProtectedParts()) {
                    int p = -1;
                    while ((p = text.indexOf(pp.getTextInSourceSegment(), p + 1)) >= 0) {
                        if (p <= off && off < p + pp.getTextInSourceSegment().length()) {
                            p += segment.getStartPosition();
                            select(p, p + pp.getTextInSourceSegment().length());
                            return true;
                        }
                    }
                }
            } catch (BadLocationException ex) {
            }
        }
        return false;
    }

    /**
     * Checks whether the selection & caret is inside editable text, and changes
     * their positions accordingly if not. Convenience method for
     * {@link #checkAndFixCaret(boolean)} that always forcibly fixes the caret.
     */
    void checkAndFixCaret() {
        checkAndFixCaret(true);
    }

    /**
     * Checks whether the selection & caret is inside editable text, and changes
     * their positions accordingly if not.
     *
     * @param force
     *            When true, ignore {@link #lockCursorToInputArea} and always
     *            fix the caret even if the user has enabled free roaming
     */
    void checkAndFixCaret(boolean force) {
        if (!force && !lockCursorToInputArea) {
            return;
        }

        Document3 doc = getOmDocument();
        if (doc == null) {
            // doc is not active
            return;
        }
        if (!doc.isEditMode()) {
            return;
        }

        // int pos = m_editor.getCaretPosition();
        int spos = getSelectionStart();
        int epos = getSelectionEnd();
        int start = doc.getTranslationStart();
        int end = doc.getTranslationEnd();

        if (spos != epos) {
            // dealing with a selection here - make sure it's w/in bounds
            if (spos < start) {
                fixSelectionStart(start);
            } else if (spos > end) {
                fixSelectionStart(end);
            }
            if (epos > end) {
                fixSelectionEnd(end);
            } else if (epos < start) {
                fixSelectionStart(start);
            }
        } else {
            // non selected text
            if (spos < start) {
                setCaretPosition(start);
            } else if (spos > end) {
                setCaretPosition(end);
            }
        }
    }

    /**
     * Need to use own implementation, because standard method moves caret at
     * the end.
     */
    private void fixSelectionStart(int start) {
        if (getCaretPosition() <= start) {
            // caret at the left - mark from ent to start
            setCaretPosition(getSelectionEnd());
            moveCaretPosition(start);
        } else {
            setSelectionStart(start);
        }
    }

    /**
     * Need to use own implementation, because standard method moves caret at
     * the end.
     */
    private void fixSelectionEnd(int end) {
        setSelectionEnd(end);
    }

    /**
     * Allow to paste into segment, even selection outside editable segment. In
     * this case selection will be truncated into segment's boundaries.
     */
    @Override
    public void paste() {
        checkAndFixCaret();
        super.paste();
    }

    /**
     * Allow to cut segment, even selection outside editable segment. In this
     * case selection will be truncated into segment's boundaries.
     */
    @Override
    public void cut() {
        checkAndFixCaret();
        super.cut();
    }

    /**
     * Remove invisible direction chars on the copy text into clipboard.
     */
    @Override
    public String getSelectedText() {
        String st = super.getSelectedText();
        return st != null ? EditorUtils.removeDirectionChars(st) : null;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        int pos = EditorTextArea3.this.viewToModel2D(event.getPoint());
        int s = controller.getSegmentIndexAtLocation(pos);
        return s < 0 ? null : controller.markerController.getToolTips(s, pos);
    }

    /**
     * Factory for create own view.
     */
    public static final ViewFactory FACTORY3 = new ViewFactory() {
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new ViewLabel(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new ViewParagraph(elem);
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }

            // default to text display
            return new ViewLabel(elem);
        }
    };

    private static class PopupMenuConstructorInfo {
        final int priority;
        final IPopupMenuConstructor constructor;

        PopupMenuConstructorInfo(int priority, IPopupMenuConstructor constructor) {
            this.priority = priority;
            this.constructor = constructor;
        }
    }

    @Override
    public void replaceSelection(String content) {
        if (lockListener.isLocked != null) {
            Core.getMainWindow().showStatusMessageRB("MW_SEGMENT_LOCKED", lockListener.isLocked);
            if (Preferences.isPreferenceDefault(Preferences.SUPPORT_LOCKED_SEGMENTS, true)) {
                return;
            }
        }
        // Overwrite current selection, and if at the end of the segment, allow
        // inserting new text.
        if (isEditable() && overtypeMode && getSelectionStart() == getSelectionEnd()
                && getCaretPosition() < getOmDocument().getTranslationEnd()) {
            int pos = getCaretPosition();
            int lastPos = Math.min(getDocument().getLength(), pos + content.length());
            select(pos, lastPos);
        }
        super.replaceSelection(content);
    }

    /** Get the caret width from the size of the current letter. */
    private int getCaretWidth() {
        FontMetrics fm = getFontMetrics(getFont());
        int carWidth = 1;
        try {
            carWidth = fm.stringWidth(getText(getCaretPosition(), 1));
        } catch (BadLocationException e) {
            /* empty */
        }
        return carWidth;
    }

    private class OvertypeCaret extends DefaultCaret {
        @Override
        public void paint(Graphics g) {
            if (overtypeMode) {
                int caretWidth = getCaretWidth();
                putClientProperty("caretWidth", caretWidth);
                g.setXORMode(Styles.EditorColor.COLOR_FOREGROUND.getColor());
                g.translate(caretWidth / 2, 0);
                super.paint(g);
            } else {
                super.paint(g);
            }
        }

        @Override
        protected synchronized void damage(Rectangle r) {
            if (overtypeMode) {
                if (r != null) {
                    int damageWidth = getCaretWidth();
                    x = r.x - 4 - (damageWidth / 2);
                    y = r.y;
                    width = 9 + 3 * damageWidth / 2;
                    height = r.height;
                    repaint();
                }
            } else {
                super.damage(r);
            }
        }
    }
}
