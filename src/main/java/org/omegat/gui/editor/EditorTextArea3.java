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
               2026 Stephan Pakebusch
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.swing.JEditorPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import org.jetbrains.annotations.Nullable;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.autocompleter.AutoCompleter;
import org.omegat.gui.editor.autocompleter.IAutoCompleter;
import org.omegat.gui.shortcuts.PropertiesShortcuts;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.TagUtil;
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
    private static final KeyStroke KEYSTROKE_DELETE_TO_SEGMENT_START = PropertiesShortcuts
            .getEditorShortcuts().getKeyStroke("editorDeleteToSegmentStart");
    private static final KeyStroke KEYSTROKE_DELETE_TO_SEGMENT_END = PropertiesShortcuts
            .getEditorShortcuts().getKeyStroke("editorDeleteToSegmentEnd");
    private static final KeyStroke KEYSTROKE_DELETE_NEXT_TOKEN_ALTERNATE = PropertiesShortcuts
            .getEditorShortcuts().getKeyStroke("editorDeleteNextTokenAlternate");
    private static final KeyStroke KEYSTROKE_MOVE_TO_SEGMENT_START = PropertiesShortcuts
            .getEditorShortcuts().getKeyStroke("editorMoveToSegmentStart");
    private static final KeyStroke KEYSTROKE_MOVE_TO_SEGMENT_END = PropertiesShortcuts
            .getEditorShortcuts().getKeyStroke("editorMoveToSegmentEnd");
    private static final KeyStroke KEYSTROKE_MOVE_TO_SEGMENT_START_SEL = PropertiesShortcuts
            .getEditorShortcuts().getKeyStroke("editorMoveToSegmentStartWithSelection");
    private static final KeyStroke KEYSTROKE_MOVE_TO_SEGMENT_END_SEL = PropertiesShortcuts
            .getEditorShortcuts().getKeyStroke("editorMoveToSegmentEndWithSelection");
    private static final KeyStroke KEYSTROKE_INSERT_NEXT_PLACEABLE = PropertiesShortcuts
            .getEditorShortcuts().getKeyStroke("editorInsertNextMissingPlaceable");
    private static final KeyStroke KEYSTROKE_INSERT_PREV_PLACEABLE = PropertiesShortcuts
            .getEditorShortcuts().getKeyStroke("editorInsertPrevMissingPlaceable");
    private static final KeyStroke KEYSTROKE_INSERT_NBSP = PropertiesShortcuts.getEditorShortcuts()
            .getKeyStroke("editorInsertNonBreakingSpace");
    private static final KeyStroke KEYSTROKE_INSERT_TAG_PAIR = PropertiesShortcuts.getEditorShortcuts()
            .getKeyStroke("editorInsertMissingTagPair");
    private static final KeyStroke KEYSTROKE_MOVE_TOKEN_PREV = PropertiesShortcuts.getEditorShortcuts()
            .getKeyStroke("editorMoveTokenPrev");
    private static final KeyStroke KEYSTROKE_MOVE_TOKEN_NEXT = PropertiesShortcuts.getEditorShortcuts()
            .getKeyStroke("editorMoveTokenNext");

    /** Undo Manager to store edits */
    protected final TranslationUndoManager undoManager = new TranslationUndoManager(this);

    protected final EditorController controller;

    protected final List<PopupMenuConstructorInfo> popupConstructors = new ArrayList<>();

    protected @Nullable String currentWord;

    protected transient IAutoCompleter autoCompleter;

    /**
     * Whether or not we are confining the cursor to the editable part of the
     * text area. The user can optionally allow the caret to roam freely.
     *
     * @see #checkAndFixCaret(boolean)
     */
    protected boolean lockCursorToInputArea = true;

    /**
     * Flag indicating if the editor is in Insert (false) or Overwrite (true)
     * mode.
     */
    protected boolean overtypeMode = false;

    private Locale targetLocale;
    private Locale sourceLocale;

    public EditorTextArea3(EditorController controller) {
        this.controller = controller;
        setEditorKit(new StyledEditorKit() {
            @Override
            public ViewFactory getViewFactory() {
                return FACTORY3;
            }

            @Override
            protected void createInputAttributes(Element element, MutableAttributeSet set) {
                set.removeAttributes(set);
                EditorController c = EditorTextArea3.this.controller;
                try {
                    c.m_docSegList[c.displayedEntryIndex].createInputAttributes(element, set);
                } catch (Exception ignored) {
                }
            }
        });

        addMouseListener(mouseListener);

        // Custom caret for overtype mode
        OvertypeCaret c = new OvertypeCaret();
        c.setBlinkRate(getCaret().getBlinkRate());
        setCaret(c);

        sourceLocale = getLocale();
        targetLocale = getLocale();

        addCaretListener(e -> {
            try {
                // Detection of target string locale.
                // It uses a source or a target language as a processing locale.
                Locale locale = isInActiveTranslation(e.getMark()) ? targetLocale : sourceLocale;
                int start = EditorUtils.getWordStart(EditorTextArea3.this, e.getMark(), locale);
                int end = EditorUtils.getWordEnd(EditorTextArea3.this, e.getMark(), locale);
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
                Log.logErrorRB(ex, "ETA_ERROR_BAD_LOCATION");
            }
        });
        setToolTipText("");
        setDragEnabled(true);
        applyColors();
        CoreEvents.registerColorsChangedEventListener(this::applyColors);

        updateLockInsertMessage();
        autoCompleter = new AutoCompleter(this);
    }

    /**
     * Apply the editor's foreground, caret and background colours from the
     * current preferences, so a colour change takes effect without a restart.
     */
    public void applyColors() {
        setForeground(Styles.EditorColor.COLOR_FOREGROUND.getColor());
        setCaretColor(Styles.EditorColor.COLOR_FOREGROUND.getColor());
        setBackground(Styles.EditorColor.COLOR_BACKGROUND.getColor());
        Document3 doc = getOmDocument();
        if (doc != null) {
            doc.applyDefaultColors();
        }
        // span colors are bound to the palette and resolve when painting
        // (see Styles#createBoundAttributeSet) — a repaint shows them
        repaint();
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        Document3 doc = getOmDocument();
        if (doc != null) {
            doc.setFont(font);
        }
    }

    void setTargetLocale(Locale targetLocale) {
        this.targetLocale = targetLocale;
    }

    void setSourceLocale(Locale sourceLocale) {
        this.sourceLocale = sourceLocale;
    }

    /**
     * Return OmDocument instead just a Document. If the editor was not
     * initialized with OmDocument, it will contain another Document
     * implementation. In this case, we don't need it.
     */
    public @Nullable Document3 getOmDocument() {
        Document doc = getDocument();
        if (doc instanceof Document3) {
            return (Document3) doc;
        }
        return null;
    }

    /**
     * Check the specified position is within the active translation.
     * 
     * @param position
     *            caret position
     * @return true, when caret is in active translation, otherwise return false
     */
    public boolean isInActiveTranslation(int position) {
        Document3 doc = getOmDocument();
        if (doc == null) {
            return false;
        }
        if (!doc.isEditMode()) {
            return false;
        }
        return (position >= doc.getTranslationStart() && position <= doc.getTranslationEnd());
    }

    protected final transient MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            autoCompleter.setVisible(false);

            boolean singleClickSegmentActivation = Preferences
                    .isPreference(Preferences.SINGLE_CLICK_SEGMENT_ACTIVATION);
            if (singleClickSegmentActivation && e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1
                    && lockCursorToInputArea) {
                int location = getCaretPosition();
                int mousepos = EditorTextArea3.this.viewToModel2D(e.getPoint());
                int segmentIndex = controller.getSegmentIndexAtLocation(location);
                int startLocation = controller.getStartForSegmentWithIndex(segmentIndex);
                int offset = mousepos - startLocation;
                boolean changed = controller.goToSegmentAtLocationAndJumpToOffset(mousepos, offset);
                if (changed) {
                    return;
                }
            }
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
        }

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
            /*
             * Copy constructors - for disable blocking in the procesing time.
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
            popupConstructors.sort(Comparator.comparingInt(o -> o.priority));
        }
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
            // key typed
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
            // Context Menu key for contextual (right-click) menu (Shift+Esc on
            // Mac)
            JPopupMenu popup = makePopupMenu(getCaretPosition());
            if (popup.getComponentCount() > 0) {
                popup.show(EditorTextArea3.this, (int) getCaret().getMagicCaretPosition().getX(),
                        (int) getCaret().getMagicCaretPosition().getY());
                processed = true;
            }
        } else if (s.equals(KEYSTROKE_NEXT)) {
            // Advance when 'Use TAB to advance'
            if (controller.settings.isUseTabForAdvance()) {
                controller.nextEntry();
                processed = true;
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
        } else if (s.equals(KEYSTROKE_DELETE_NEXT_TOKEN)
                || s.equals(KEYSTROKE_DELETE_NEXT_TOKEN_ALTERNATE)) {
            // Delete next token; the alternate binding serves keyboards
            // without a forward-delete key
            try {
                processed = wholeTagDelete(true);
                if (!processed) {
                    int offset = getCaretPosition();
                    int nextWord = Utilities.getNextWord(this, offset);
                    setSelectionStart(offset);
                    if (doc != null) {
                        int c = Math.min(nextWord, doc.getTranslationEnd());
                        setSelectionEnd(c);
                    } else {
                        setSelectionEnd(nextWord);
                    }
                    replaceSelection("");

                    processed = true;
                }
            } catch (BadLocationException ex) {
                // do nothing
            }
        } else if (s.equals(KEYSTROKE_DELETE_TO_SEGMENT_START)) {
            processed = deleteToSegmentBound(false);
        } else if (s.equals(KEYSTROKE_DELETE_TO_SEGMENT_END)) {
            processed = deleteToSegmentBound(true);
        } else if (s.equals(KEYSTROKE_MOVE_TO_SEGMENT_START)) {
            processed = moveToSegmentBound(false, false);
        } else if (s.equals(KEYSTROKE_MOVE_TO_SEGMENT_END)) {
            processed = moveToSegmentBound(true, false);
        } else if (s.equals(KEYSTROKE_MOVE_TO_SEGMENT_START_SEL)) {
            processed = moveToSegmentBound(false, true);
        } else if (s.equals(KEYSTROKE_MOVE_TO_SEGMENT_END_SEL)) {
            processed = moveToSegmentBound(true, true);
        } else if (s.equals(KEYSTROKE_INSERT_NEXT_PLACEABLE)) {
            processed = insertMissingPlaceable(true);
        } else if (s.equals(KEYSTROKE_INSERT_PREV_PLACEABLE)) {
            processed = insertMissingPlaceable(false);
        } else if (s.equals(KEYSTROKE_INSERT_NBSP)) {
            processed = insertNonBreakingSpace();
        } else if (s.equals(KEYSTROKE_INSERT_TAG_PAIR)) {
            processed = insertMissingTagPair();
        } else if (s.equals(KEYSTROKE_MOVE_TOKEN_PREV)) {
            processed = moveTokenAtCaret(false);
        } else if (s.equals(KEYSTROKE_MOVE_TOKEN_NEXT)) {
            processed = moveTokenAtCaret(true);
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
            super.processKeyEvent(e);
            // Note that the translation start/end position are not updated yet.
            // This has been updated when key-released event occurs.
        }

        // some after-processing catches
        if (!processed && e.getKeyChar() != 0 && isNavigationKey(e.getKeyCode())) {
            // if caret is moved over existing chars, check and fix caret
            // position
            // works only in after-processing if translation length (start and
            // end position) has not changed,
            // because start and end position are not updated yet.
            checkAndFixCaret(false);
            autoCompleter.updatePopup(true);
        }
    }

    /**
     * Delete from the segment bound to the caret: everything before the
     * caret ({@code toEnd} false) or everything after it ({@code toEnd}
     * true), clamped to the editable range. A live selection is deleted
     * instead, so a slipped modifier never wipes more than the visible
     * selection. Always consumes the event, even as a no-op: the mac
     * binding shadows a native Cocoa gesture, and letting the event fall
     * through would put two semantics on one key.
     */
    private boolean deleteToSegmentBound(boolean toEnd) {
        Document3 doc = getOmDocument();
        if (doc == null || !doc.isEditMode()) {
            return true;
        }
        int start = doc.getTranslationStart();
        int end = doc.getTranslationEnd();
        int selStart = getSelectionStart();
        int selEnd = getSelectionEnd();
        if (selStart != selEnd) {
            selStart = Math.max(selStart, start);
            selEnd = Math.min(selEnd, end);
        } else {
            int caret = getCaretPosition();
            if (caret < start || caret > end) {
                return true;
            }
            selStart = toEnd ? caret : start;
            selEnd = toEnd ? end : caret;
        }
        if (selStart >= selEnd) {
            return true;
        }
        int caretBefore = getCaretPosition();
        int lengthBefore = getDocument().getLength();
        int rangeStart = selStart;
        int rangeEnd = selEnd;
        // pause only the undo bookkeeping and record one snapshot with the
        // caret position of the moment the shortcut fired: the automatic
        // snapshot would stamp the end of the removed range, making an undo
        // jump to the segment end instead of back to where the user was
        undoManager.runAtomic(() -> {
            setSelectionStart(rangeStart);
            setSelectionEnd(rangeEnd);
            replaceSelection("");
        }, caretBefore - start);
        if (getDocument().getLength() == lengthBefore) {
            // the range touched a protected tag and the document filter
            // rejected the removal: collapse the leftover selection so the
            // editor does not sit on a dead, boundary-crossing selection
            setCaretPosition(caretBefore);
        }
        autoCompleter.updatePopup(true);
        return true;
    }

    /**
     * Move the caret to the start or end of the editable range, optionally
     * extending the selection. The document outside the active segment is
     * not editable, so this is the segment-scoped reading of the native
     * "to start/end of document" gestures.
     */
    private boolean moveToSegmentBound(boolean toEnd, boolean extendSelection) {
        Document3 doc = getOmDocument();
        if (doc == null || !doc.isEditMode()) {
            return true;
        }
        int target = toEnd ? doc.getTranslationEnd() : doc.getTranslationStart();
        if (extendSelection) {
            moveCaretPosition(target);
        } else {
            setCaretPosition(target);
        }
        autoCompleter.updatePopup(true);
        return true;
    }

    /**
     * Insert the first (or last) placeable of the source text that is still
     * missing from the translation: tags and other protected parts, URLs,
     * e-mail addresses, numbers. The missing list is recomputed on every
     * call, so pressing repeatedly works through it.
     */
    private boolean insertMissingPlaceable(boolean fromStart) {
        Document3 doc = getOmDocument();
        if (doc == null || !doc.isEditMode()) {
            return true;
        }
        SourceTextEntry ste = doc.getController().getCurrentEntry();
        String translation = doc.extractTranslation();
        if (ste == null || translation == null) {
            return true;
        }
        int start = doc.getTranslationStart();
        int end = doc.getTranslationEnd();
        int selStart = Math.max(getSelectionStart(), start);
        int selEnd = Math.min(getSelectionEnd(), end);
        if (selStart < selEnd) {
            // the insertion replaces the selection, so its content must not
            // count as present when computing what is missing
            translation = translation.substring(0, selStart - start) + translation.substring(selEnd - start);
        }
        List<String> protectedTexts = protectedTexts(ste);
        List<String> missing = SegmentEditingOps.missingPlaceables(ste.getSrcText(), translation,
                protectedTexts);
        if (missing.isEmpty()) {
            return true;
        }
        String insertion = fromStart ? missing.get(0) : missing.get(missing.size() - 1);
        // the canonical insert path wraps tags in bidi controls for RTL
        // targets and fixes the caret before inserting
        controller.insertText(insertion);
        autoCompleter.updatePopup(true);
        return true;
    }

    private static List<String> protectedTexts(SourceTextEntry ste) {
        List<String> result = new ArrayList<>();
        for (ProtectedPart pp : ste.getProtectedParts()) {
            result.add(pp.getTextInSourceSegment());
        }
        return result;
    }

    /**
     * Insert the first missing tag pair of the segment: around the selection
     * when one exists, otherwise at the caret with the caret placed between
     * the tags (SF-1302).
     */
    private boolean insertMissingTagPair() {
        Document3 doc = getOmDocument();
        if (doc == null || !doc.isEditMode() || doc.getController().getCurrentEntry() == null) {
            return true;
        }
        String[] pair = SegmentEditingOps.firstMissingTagPair(TagUtil.getGroupedMissingTagsFromTarget(),
                TagUtil.TAG_SEPARATOR_SENTINEL);
        if (pair == null) {
            return true;
        }
        int start = doc.getTranslationStart();
        int end = doc.getTranslationEnd();
        int selStart = Math.max(getSelectionStart(), start);
        int selEnd = Math.min(getSelectionEnd(), end);
        if (selStart < selEnd) {
            // wrap the (clamped) selection: one insertText call keeps the
            // bidi handling of the canonical path, runAtomic keeps the
            // remove+insert pair a single undo step
            int caretBefore = getCaretPosition();
            String wrapped;
            try {
                wrapped = pair[0] + getDocument().getText(selStart, selEnd - selStart) + pair[1];
            } catch (BadLocationException ex) {
                return true;
            }
            select(selStart, selEnd);
            undoManager.runAtomic(() -> controller.insertText(wrapped),
                    Math.min(Math.max(caretBefore, start), end) - start);
        } else {
            if (getSelectionStart() != getSelectionEnd()) {
                // selection lies entirely outside the editable range:
                // collapse it so insertText cannot replace protected text
                setCaretPosition(Math.min(Math.max(getCaretPosition(), start), end));
            }
            controller.insertText(pair[0] + pair[1]);
            // land the caret between the tags; search backwards so any bidi
            // controls the canonical path added around the tags are skipped
            String translation = doc.extractTranslation();
            if (translation != null) {
                int posRel = translation.lastIndexOf(pair[1],
                        Math.min(getCaretPosition() - start, translation.length()));
                if (posRel >= 0) {
                    setCaretPosition(start + posRel);
                }
            }
        }
        autoCompleter.updatePopup(true);
        return true;
    }

    /** Insert a no-break space (U+00A0) at the caret. */
    private boolean insertNonBreakingSpace() {
        Document3 doc = getOmDocument();
        if (doc == null || !doc.isEditMode()) {
            return true;
        }
        controller.insertText("\u00A0");
        autoCompleter.updatePopup(true);
        return true;
    }

    /**
     * Swap the token at the caret with its neighbour in logical order,
     * keeping the caret on the moved token so repeated presses walk the
     * token through the segment. One document mutation, one undo step.
     */
    private boolean moveTokenAtCaret(boolean forward) {
        Document3 doc = getOmDocument();
        if (doc == null || !doc.isEditMode()) {
            return true;
        }
        int start = doc.getTranslationStart();
        int end = doc.getTranslationEnd();
        int caret = getCaretPosition();
        if (caret < start || caret > end) {
            return true;
        }
        String translation = doc.extractTranslation();
        SourceTextEntry ste = doc.getController().getCurrentEntry();
        if (translation == null || ste == null) {
            return true;
        }
        Locale locale = targetLocale != null ? targetLocale : Locale.getDefault();
        SegmentEditingOps.TokenSwap swap = SegmentEditingOps.computeTokenSwap(translation, caret - start,
                forward, locale, protectedTexts(ste));
        if (swap == null) {
            return true;
        }
        // the swap replaces a non-empty region with non-empty text, which
        // the undo manager would record as two snapshots (remove + insert);
        // run it atomically with the pre-swap caret, so an undo returns the
        // caret to where the user was, not to the moved token
        undoManager.runAtomic(() -> {
            setSelectionStart(start + swap.regionStart);
            setSelectionEnd(start + swap.regionEnd);
            replaceSelection(swap.replacement);
        }, caret - start);
        if (swap.replacement.equals(doc.extractTranslation().substring(swap.regionStart,
                swap.regionStart + swap.replacement.length()))) {
            setCaretPosition(start + swap.caretAfter);
        } else {
            // the document filter rejected the replacement (tag protection):
            // collapse the leftover selection back to the original caret
            setCaretPosition(caret);
        }
        autoCompleter.updatePopup(true);
        return true;
    }

    private void updateLockInsertMessage() {
        String lock = lockCursorToInputArea ? OStrings.getString("MW_STATUS_CURSOR_LOCK_ON")
                : OStrings.getString("MW_STATUS_CURSOR_LOCK_OFF");
        String ins = overtypeMode ? OStrings.getString("MW_STATUS_CURSOR_OVERTYPE_ON")
                : OStrings.getString("MW_STATUS_CURSOR_OVERTYPE_OFF");
        String lockTip = lockCursorToInputArea ? OStrings.getString("MW_STATUS_TIP_CURSOR_LOCK_ON")
                : OStrings.getString("MW_STATUS_TIP_CURSOR_LOCK_OFF");
        String insTip = overtypeMode ? OStrings.getString("MW_STATUS_TIP_CURSOR_OVERTYPE_ON")
                : OStrings.getString("MW_STATUS_TIP_CURSOR_OVERTYPE_OFF");
        Core.getMainWindow().showLockInsertMessage(lock + " | " + ins, lockTip + " | " + insTip);
    }

    private boolean switchOvertypeMode() {
        boolean switchOvertypeMode = !overtypeMode;
        overtypeMode = switchOvertypeMode;

        if (overtypeMode) {
            // Change the caret shape, width and color
            setCaretColor(Styles.EditorColor.COLOR_BACKGROUND.getColor());
            putClientProperty("caretWidth", getCaretWidth());

            // We need to force the caret damage to have the rectangle to
            // correctly show up, otherwise half of the caret is shown.
            try {
                OvertypeCaret caret = (OvertypeCaret) getCaret();
                Rectangle r = modelToView2D(caret.getDot()).getBounds();
                caret.damage(r);
            } catch (BadLocationException e) {
                Log.log(e);
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
            // We are at the edge of the translation but moving toward the
            // outside.
            // Don't try to jump over tags.
            return false;
        }
        SourceTextEntry ste = doc.getController().getCurrentEntry();
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
        SourceTextEntry ste = doc.getController().getCurrentEntry();
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
        SourceTextEntry ste = getOmDocument().getController().getCurrentEntry();
        if (ste != null) {
            try {
                String text = getOmDocument().getText(segment.getStartPosition(),
                        segment.getEndPosition() - segment.getStartPosition());
                int off = pos - segment.getStartPosition();
                if (off >= 0 && off < text.length()) {
                    for (ProtectedPart pp : ste.getProtectedParts()) {
                        if (findAndSelectPart(text, off, pp, segment.getStartPosition())) {
                            return true;
                        }
                    }
                }
            } catch (BadLocationException ex) {
                Log.log(ex);
            }
        }
        return false;
    }

    private boolean findAndSelectPart(String text, int off, ProtectedPart pp, int segmentStart) {
        String protectedPartText = pp.getTextInSourceSegment();
        if (protectedPartText == null) {
            return false;
        }
        int p = -1;
        while ((p = text.indexOf(protectedPartText, p + 1)) >= 0) {
            if (p <= off && off < p + protectedPartText.length()) {
                int start = expandSelectionStart(text, p);
                int end = expandSelectionEnd(text, p + protectedPartText.length());

                select(start + segmentStart, end + segmentStart);
                return true;
            }
        }
        return false;
    }

    private int expandSelectionStart(String text, int start) {
        // expand selection if surrounded by bidi marks (up to 2 chars)
        for (int i = 0; i < 2; i++) {
            if (start > 0 && isDirectionChar(text.charAt(start - 1))) {
                start--;
            }
        }
        return start;
    }

    private int expandSelectionEnd(String text, int end) {
        // expand selection if surrounded by bidi marks (up to 2 chars)
        for (int i = 0; i < 2; i++) {
            if (end < text.length() && isDirectionChar(text.charAt(end))) {
                end++;
            }
        }
        return end;
    }

    private boolean isDirectionChar(char ch) {
        return ch == '\u200E' || ch == '\u200F' || ch == '\u202A' || ch == '\u202B' || ch == '\u202C';
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
    public @Nullable String getSelectedText() {
        String st = super.getSelectedText();
        return st != null ? EditorUtils.removeDirectionChars(st) : null;
    }

    @Override
    public @Nullable String getToolTipText(MouseEvent event) {
        int pos = EditorTextArea3.this.viewToModel2D(event.getPoint());
        int s = controller.getSegmentIndexAtLocation(pos);
        return s < 0 ? null : controller.markerController.getToolTips(s, pos);
    }

    /**
     * Factory for create own view.
     */
    public static final ViewFactory FACTORY3 = new ViewFactory() {
        @Override
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

    protected static class PopupMenuConstructorInfo {
        final int priority;
        final IPopupMenuConstructor constructor;

        PopupMenuConstructorInfo(int priority, IPopupMenuConstructor constructor) {
            this.priority = priority;
            this.constructor = constructor;
        }
    }

    @Override
    public void replaceSelection(String content) {
        // Overwrite the current selection, and if at the end of the segment,
        // allow inserting new text.
        var omDocument = getOmDocument();
        if (isEditable() && omDocument != null && overtypeMode && getSelectionStart() == getSelectionEnd()
                && getCaretPosition() < omDocument.getTranslationEnd()) {
            int pos = getCaretPosition();
            int lastPos = Math.min(omDocument.getTranslationEnd(), pos + content.length());
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
