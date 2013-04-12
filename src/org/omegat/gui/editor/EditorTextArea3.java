/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2009 Didier Briel
               2010 Wildrich Fourie
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

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JPopupMenu;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.undo.UndoManager;

import org.omegat.core.CoreEvents;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.DockingUI;

/**
 * Changes of standard JEditorPane implementation for support custom behavior.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Wildrich Fourie
 */
@SuppressWarnings("serial")
public class EditorTextArea3 extends JEditorPane {

    /** Undo Manager to store edits */
    protected final UndoManager undoManager = new UndoManager();

    protected final EditorController controller;

    protected final List<PopupMenuConstructorInfo> popupConstructors = new ArrayList<PopupMenuConstructorInfo>();

    protected String currentWord;

    public EditorTextArea3(EditorController controller) {
        this.controller = controller;
        setEditorKit(new StyledEditorKit() {
            public ViewFactory getViewFactory() {
                return factory3;
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

        addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
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
            }
        });
        setToolTipText("");
    }

    /** Orders to cancel all Undoable edits. */
    public void cancelUndo() {
        undoManager.die();
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

    protected MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            // where is the mouse
            int mousepos = viewToModel(e.getPoint());
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                controller.goToSegmentAtLocation(mousepos);
            }
            if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                PopupMenuConstructorInfo[] cons;
                synchronized (popupConstructors) {
                    /**
                     * Copy constructors - for disable blocking in the procesing
                     * time.
                     */
                    cons = popupConstructors.toArray(new PopupMenuConstructorInfo[popupConstructors.size()]);
                }

                boolean isInActiveTranslation = mousepos >= getOmDocument().getTranslationStart()
                        && mousepos <= getOmDocument().getTranslationEnd();
                boolean isInActiveEntry;
                int ae = controller.displayedEntryIndex;
                SegmentBuilder sb = controller.m_docSegList[ae];
                if (sb.isActive()) {
                    isInActiveEntry = mousepos >= sb.getStartPosition() && mousepos <= sb.getEndPosition();
                } else {
                    isInActiveEntry = false;
                }

                JPopupMenu popup = new JPopupMenu();
                for (PopupMenuConstructorInfo c : cons) {
                    // call each constructor
                    c.constructor.addItems(popup, EditorTextArea3.this, mousepos, isInActiveEntry,
                            isInActiveTranslation, sb);
                }

                DockingUI.removeUnusedMenuSeparators(popup);

                if (popup.getComponentCount() > 0) {
                    popup.show(EditorTextArea3.this, (int) e.getPoint().getX(), (int) e.getPoint().getY());
                }
            }
        }
    };

    /**
     * Add new constructor into list and sort full list by priority.
     */
    protected void registerPopupMenuConstructors(int priority, IPopupMenuConstructor constructor) {
        synchronized (popupConstructors) {
            popupConstructors.add(new PopupMenuConstructorInfo(priority, constructor));
            Collections.sort(popupConstructors, new Comparator<PopupMenuConstructorInfo>() {
                public int compare(PopupMenuConstructorInfo o1, PopupMenuConstructorInfo o2) {
                    return o1.priority - o2.priority;
                }
            });
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
            // key released. Only now the translation start/end positions have been updated according to keydown events
            reformatTranslation();
            super.processKeyEvent(e);
            return;
        } else if (keyEvent == KeyEvent.KEY_TYPED) {
            //key typed
            super.processKeyEvent(e);
            return;
        }

        boolean processed = false;

        boolean mac = StaticUtils.onMacOSX();

        Document3 doc = getOmDocument();

        // non-standard processing
        if (isKey(e, KeyEvent.VK_TAB, 0)) {
            // press TAB when 'Use TAB to advance'
            if (controller.settings.isUseTabForAdvance()) {
                controller.nextEntry();
                processed = true;
            }
        } else if (isKey(e, KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK)) {
            // press Shift+TAB when 'Use TAB to advance'
            if (controller.settings.isUseTabForAdvance()) {
                controller.prevEntry();
                processed = true;
            }
        } else if (isKey(e, KeyEvent.VK_ENTER, 0)) {
            // press ENTER
            if (!controller.settings.isUseTabForAdvance()) {
                controller.nextEntry();
                processed = true;
            } else {
                processed = true;
            }
        } else if ((!mac && isKey(e, KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK))
                || (mac && isKey(e, KeyEvent.VK_ENTER, KeyEvent.META_MASK))) {
            // press Ctrl+ENTER (Cmd+Enter for MacOS)
            if (!controller.settings.isUseTabForAdvance()) {
                controller.prevEntry();
                processed = true;
            }
        } else if (isKey(e, KeyEvent.VK_ENTER, KeyEvent.SHIFT_MASK)) {
            // convert Shift+Enter event to straight enter key
            KeyEvent ke = new KeyEvent(e.getComponent(), e.getID(), e.getWhen(), 0, KeyEvent.VK_ENTER, '\n');
            super.processKeyEvent(ke);
            processed = true;
        } else if ((!mac && isKey(e, KeyEvent.VK_A, KeyEvent.CTRL_MASK))
                || (mac && isKey(e, KeyEvent.VK_A, KeyEvent.META_MASK))) {
            // handling Ctrl+A manually (Cmd+A for MacOS)
            setSelectionStart(doc.getTranslationStart());
            setSelectionEnd(doc.getTranslationEnd());
            processed = true;
        } else if (isKey(e, KeyEvent.VK_O, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK)) {
            // handle Ctrl+Shift+O - toggle orientation LTR-RTL
            Cursor oldCursor = this.getCursor();
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            controller.toggleOrientation();
            this.setCursor(oldCursor);
            processed = true;
        } else if ((!mac && isKey(e, KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_MASK))
                || (mac && isKey(e, KeyEvent.VK_BACK_SPACE, KeyEvent.ALT_MASK))) {
            // handle Ctrl+Backspace (Alt+Backspace for MacOS)
            try {
                int offset = getCaretPosition();
                int prevWord = Utilities.getPreviousWord(this, offset);
                int c = Math.max(prevWord, doc.getTranslationStart());
                setSelectionStart(c);
                setSelectionEnd(offset);
                replaceSelection("");

                processed = true;
            } catch (BadLocationException ex) {
                // do nothing
            }
        } else if ((!mac && isKey(e, KeyEvent.VK_DELETE, KeyEvent.CTRL_MASK))
                || (mac && isKey(e, KeyEvent.VK_DELETE, KeyEvent.ALT_MASK))) {
            // handle Ctrl+Backspace (Alt+Delete for MacOS)
            try {
                int offset = getCaretPosition();
                int nextWord = Utilities.getNextWord(this, offset);
                int c = Math.min(nextWord, doc.getTranslationEnd());
                setSelectionStart(offset);
                setSelectionEnd(c);
                replaceSelection("");

                processed = true;
            } catch (BadLocationException ex) {
                // do nothing
            }
        } else if ((!mac && isKey(e, KeyEvent.VK_PAGE_UP, KeyEvent.CTRL_MASK))
                || (mac && isKey(e, KeyEvent.VK_PAGE_UP, KeyEvent.META_MASK))) {
            // Ctrl+PgUp - to the begin of document(Cmd+PgUp for MacOS)
            setCaretPosition(0);
            processed = true;
        } else if ((!mac && isKey(e, KeyEvent.VK_PAGE_DOWN, KeyEvent.CTRL_MASK))
                || (mac && isKey(e, KeyEvent.VK_PAGE_DOWN, KeyEvent.META_MASK))) {
            // Ctrl+PgDn - to the end of document(Cmd+PgDn for MacOS)
            setCaretPosition(getOmDocument().getLength());
            processed = true;
        }

        // leave standard processing if need
        if (processed) {
            e.consume();
        } else {
            if ((e.getModifiers() & (KeyEvent.CTRL_MASK | KeyEvent.META_MASK | KeyEvent.ALT_MASK)) == 0) {
                // there is no Alt,Ctrl,Cmd keys, i.e. it's char
                if (e.getKeyCode() != KeyEvent.VK_SHIFT) {
                    // it's not a single 'shift' press
                    // fix caret position prior to inserting character
                    checkAndFixCaret();
                }
            }
            super.processKeyEvent(e);
            //note that the translation start/end position are not updated yet. This has been updated when then keyreleased event occurs. 
        }

        // some after-processing catches
        if (!processed && e.getKeyChar() != 0) {
            switch (e.getKeyCode()) {
                //if caret is moved over existing chars, check and fix caret position
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
                    checkAndFixCaret(); //works only in after-processing if translation length (start and end position) has not changed, because start and end position are not updated yet.
            }
        }
    }

    /**
     * Formats placeholders in translation
     */
    private void reformatTranslation() {
        Document3 doc = getOmDocument();
        String trans = doc.extractTranslation();

        //there are issues when formatting rtl text. Don't do it, we use markers in that case.
        //for slightly better performance, check here on orientation.
        if (trans != null && controller.currentOrientation == Document3.ORIENTATION.ALL_LTR) {
            //prevent the formatting to become an undoable thing, by removing the undomanager temporary
            getOmDocument().removeUndoableEditListener(undoManager);

            int start = doc.getTranslationStart();
            int end = doc.getTranslationEnd();

            int ae = controller.displayedEntryIndex;
            SegmentBuilder sb = controller.m_docSegList[ae];
            sb.formatText(trans, start, end, false);

            //enable undo manager again.
            getOmDocument().addUndoableEditListener(undoManager);
        }
    }


    /**
     * Checks whether the selection & caret is inside editable text, and changes
     * their positions accordingly if not.
     */
    void checkAndFixCaret() {
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
     * Check if specified key pressed.
     * 
     * @param e
     *            pressed key event
     * @param code
     *            required key code
     * @param modifiers
     *            required modifiers
     * @return true if checked key pressed
     */
    private static boolean isKey(KeyEvent e, int code, int modifiers) {
        return e.getKeyCode() == code && e.getModifiers() == modifiers;
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
        int pos = viewToModel(event.getPoint());
        int s = controller.getSegmentIndexAtLocation(pos);
        return controller.markerController.getToolTips(s, pos);
    }

    /**
     * Factory for create own view.
     */
    public static ViewFactory factory3 = new ViewFactory() {
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new ViewLabel(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new ParagraphView(elem);
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

        public PopupMenuConstructorInfo(int priority, IPopupMenuConstructor constructor) {
            this.priority = priority;
            this.constructor = constructor;
        }
    }
}
