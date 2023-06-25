/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2013 Aaron Madlon-Kay, Zoltan Bartko
               2015 Aaron Madlon-Kay
               2023 Damien Rembert
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

import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Position;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.omegat.util.gui.Styles;

/**
 * We need to redefine some standard document behavior.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 * @author Zoltan Bartko
 */
@SuppressWarnings("serial")
public class Document3 extends DefaultStyledDocument {
    public enum ORIENTATION {
        /** All text is left-to-right oriented. */
        ALL_LTR,
        /** All text is right-to-left oriented. */
        ALL_RTL,
        /**
         * different texts/segments have different orientation, depending on
         * language/locale.
         */
        DIFFER
    };

    protected final EditorController controller;

    /** Position of active translation in text. */
    Position activeTranslationBeginM1;
    Position activeTranslationEndP1;

    /**
     * Flag for check internal changes of content, which should be always
     * acceptable.
     * <p>
     * Note that there is a concurrency bug with the AquaCaret class (part of
     * the OS X native LAF) whereby <i>insertion</i> into the Document while the
     * doc is visible can cause the caret to try to update itself while the doc
     * internals are inconsistent, leading to exceptions whenever any visual
     * update of the Editor is performed (the Editor becomes unusable).
     * <p>
     * This bug is very old (reported as early as February 2006) and appears to
     * not have been addressed even in December 2015 in Java 1.8.0_66), though
     * it is very hard to reproduce (it appears to be a concurrency issue that
     * only manifests itself under certain circumstances).
     * <p>
     * There is a chance that the "real" bug is in the way we are manipulating
     * the JEditorPane and/or the underlying document, but it is unclear what
     * the correct solution would be.
     * <p>
     * As a workaround, when setting this flag to true, if the changes are to
     * include insertions or deletions of text in the document, you must also
     * disable the editor's caret updates temporarily (e.g. with
     * {@code StaticUIUtils#setCaretUpdateEnabled()}). After the document
     * changes are complete and you have set this flag back to false, caret
     * update can be re-enabled.
     *
     * @see <a href="https://sourceforge.net/p/omegat/bugs/162/">Initial
     *      ticket</a>
     * @see <a href="https://sourceforge.net/p/omegat/bugs/529/">Later, more
     *      specific ticket</a>
     */
    protected boolean trustedChangesInProgress = false;

    /**
     * Flag to indicate that text is currently being composed (should not be
     * considered to have been input yet) by an IME.
     */
    protected boolean textBeingComposed = false;

    public Document3(final EditorController controller) {
        this.controller = controller;

        Style defaultStyle = getDefaultStyle();
        StyleConstants.setForeground(defaultStyle, Styles.EditorColor.COLOR_FOREGROUND.getColor());
        StyleConstants.setBackground(defaultStyle, Styles.EditorColor.COLOR_BACKGROUND.getColor());
        setFont(controller.font);
    }

    private Style getDefaultStyle() {
        StyleContext styleContext = (StyleContext) getAttributeContext();
        return styleContext.getStyle(StyleContext.DEFAULT_STYLE);
    }

    void setFont(Font font) {
        Style defaultStyle = getDefaultStyle();
        StyleConstants.setFontFamily(defaultStyle, font.getFamily());
        StyleConstants.setFontSize(defaultStyle, font.getSize());
        StyleConstants.setBold(defaultStyle, font.isBold());
        StyleConstants.setItalic(defaultStyle, font.isItalic());
    }

    /**
     * Calculate the position of the start of the current translation
     */
    public int getTranslationStart() {
        return activeTranslationBeginM1.getOffset() + 1;
    }

    /**
     * Calculate the position of the end of the current translation
     */
    protected int getTranslationEnd() {
        return activeTranslationEndP1.getOffset() - 1;
    }

    /**
     * Check if document is in edit mode, i.e. one of segment activated for
     * edit.
     */
    boolean isEditMode() {
        return activeTranslationBeginM1 != null && activeTranslationEndP1 != null;
    }

    /**
     * Stop edit mode, remove info about active translation position.
     */
    void stopEditMode() {
        activeTranslationBeginM1 = null;
        activeTranslationEndP1 = null;
    }

    /**
     * Extract active translation.
     *
     * @return active translation text
     */
    String extractTranslation() {
        if (!isEditMode()) {
            return null;
        }
        int start = getTranslationStart();
        int end = getTranslationEnd();
        try {
            return getText(start, end - start);
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Set alignment for specified part of text.
     *
     * @param beginOffset
     *            begin offset
     * @param endOffset
     *            end offset
     * @param isRightAlignment
     *            false - left alignment, true - right alignment
     */
    protected void setAlignment(int beginOffset, int endOffset, boolean isRightAlignment) {
        try {
            writeLock();

            DefaultDocumentEvent changes = new DefaultDocumentEvent(beginOffset, endOffset - beginOffset,
                    DocumentEvent.EventType.CHANGE);

            Element root = getDefaultRootElement();
            int parBeg = root.getElementIndex(beginOffset);
            int parEnd = root.getElementIndex(endOffset - 1);
            for (int par = parBeg; par <= parEnd; par++) {
                Element el = root.getElement(par);
                MutableAttributeSet attr = (MutableAttributeSet) el.getAttributes();
                attr.addAttribute(StyleConstants.Alignment,
                        isRightAlignment ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);
            }

            changes.end();
            fireChangedUpdate(changes);
        } finally {
            writeUnlock();
        }
    }
}
