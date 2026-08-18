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

import java.awt.Color;
import java.awt.Font;

import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Position;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.jetbrains.annotations.Nullable;
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

    private final EditorController controller;

    /** Position of active translation in text. */
    @Nullable
    Position activeTranslationBeginM1;
    @Nullable
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
    private boolean trustedChangesInProgress = false;

    /**
     * Flag to indicate that text is currently being composed (should not be
     * considered to have been input yet) by an IME.
     */
    private boolean textBeingComposed = false;

    boolean getTrustedChangesInProgress() {
        return trustedChangesInProgress;
    }

    void setTrustedChangesInProgress(boolean trustedChangesInProgress) {
        this.trustedChangesInProgress = trustedChangesInProgress;
    }

    boolean getTextBeingComposed() {
        return textBeingComposed;
    }

    void setTextBeingComposed(boolean textBeingComposed) {
        this.textBeingComposed = textBeingComposed;
    }

    public Document3(final EditorController controller) {
        this.controller = controller;

        applyDefaultColors();
        setFont(controller.font);
    }

    /**
     * Bake the editor-wide default colors into the default style. Kept a
     * no-op while they are unchanged, so a palette edit of span colors stays
     * a pure repaint; an actual default change lets Swing revalidate the
     * views without rebuilding the document.
     */
    void applyDefaultColors() {
        Style defaultStyle = getDefaultStyle();
        Color foreground = Styles.EditorColor.COLOR_FOREGROUND.getColor();
        Color background = Styles.EditorColor.COLOR_BACKGROUND.getColor();
        if (!foreground.equals(StyleConstants.getForeground(defaultStyle))
                || !background.equals(StyleConstants.getBackground(defaultStyle))) {
            StyleConstants.setForeground(defaultStyle, foreground);
            StyleConstants.setBackground(defaultStyle, background);
        }
    }

    /**
     * Spans bound to a palette entry resolve their colors against the
     * palette currently in effect (see
     * {@link Styles#createBoundAttributeSet}), so consumers that ask the
     * document — instead of a view — see live colors as well.
     */
    @Override
    public Color getForeground(AttributeSet attr) {
        Color bound = Styles.resolveBoundForeground(attr);
        return bound != null ? bound : super.getForeground(attr);
    }

    @Override
    public Color getBackground(AttributeSet attr) {
        Color bound = Styles.resolveBoundBackground(attr);
        return bound != null ? bound : super.getBackground(attr);
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
        if (activeTranslationBeginM1 == null) {
            return 0;
        }
        return activeTranslationBeginM1.getOffset() + 1;
    }

    /**
     * Calculate the position of the end of the current translation
     */
    protected int getTranslationEnd() {
        if (activeTranslationEndP1 == null) {
            return 0;
        }
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
    @Nullable
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
        setAlignmentValue(beginOffset, endOffset,
                isRightAlignment ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);
    }

    /** Set a specific paragraph alignment, e.g. the user-chosen centering. */
    protected void setAlignmentValue(int beginOffset, int endOffset, int alignment) {
        try {
            writeLock();

            DefaultDocumentEvent changes = new DefaultDocumentEvent(beginOffset, endOffset - beginOffset,
                    DocumentEvent.EventType.CHANGE);

            applyAlignmentAttributes(beginOffset, endOffset, alignment);

            changes.end();
            fireChangedUpdate(changes);
        } finally {
            writeUnlock();
        }
    }

    /**
     * Set a paragraph alignment without firing a change event. For batch
     * passes over the whole document: one event per part lets the view
     * updates pile up, so the batch fires a single document-wide event
     * through {@link #fireAlignmentBatchDone()} at its end.
     */
    protected void applyAlignmentValue(int beginOffset, int endOffset, int alignment) {
        try {
            writeLock();
            applyAlignmentAttributes(beginOffset, endOffset, alignment);
        } finally {
            writeUnlock();
        }
    }

    /** The one document-wide change event closing a quiet alignment batch. */
    protected void fireAlignmentBatchDone() {
        try {
            writeLock();
            DefaultDocumentEvent changes = new DefaultDocumentEvent(0, getLength(),
                    DocumentEvent.EventType.CHANGE);
            changes.end();
            fireChangedUpdate(changes);
        } finally {
            writeUnlock();
        }
    }

    private void applyAlignmentAttributes(int beginOffset, int endOffset, int alignment) {
        Element root = getDefaultRootElement();
        int parBeg = root.getElementIndex(beginOffset);
        // An empty part still owns the paragraph at its begin offset.
        int parEnd = root.getElementIndex(Math.max(beginOffset, endOffset - 1));
        for (int par = parBeg; par <= parEnd; par++) {
            Element el = root.getElement(par);
            MutableAttributeSet attr = (MutableAttributeSet) el.getAttributes();
            attr.addAttribute(StyleConstants.Alignment, alignment);
        }
    }

    protected EditorController getController() {
        return controller;
    }
}
