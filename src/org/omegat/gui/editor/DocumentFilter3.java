/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2013 Aaron Madlon-Kay, Alex Buloichik
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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.StyleConstants;

import org.jspecify.annotations.Nullable;
import org.omegat.core.data.ProtectedPart;
import org.omegat.util.Preferences;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Own implementation of DocumentFilter. It required for disable for user's edit
 * text outside translation.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public class DocumentFilter3 extends DocumentFilter {
    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        UIThreadsUtil.mustBeSwingThread();
        if (isPossible(fb.getDocument(), offset, length)) {
            super.remove(fb, offset, length);
        }
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, @Nullable AttributeSet attr)
            throws BadLocationException {
        UIThreadsUtil.mustBeSwingThread();

        if (attr != null) {
            ((Document3) fb.getDocument())
                    .setTextBeingComposed(attr.isDefined(StyleConstants.ComposedTextAttribute));
        }

        if (isPossible(fb.getDocument(), offset, 0)) {
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, @Nullable AttributeSet attrs)
            throws BadLocationException {
        UIThreadsUtil.mustBeSwingThread();

        if (attrs != null) {
            ((Document3) fb.getDocument())
                    .setTextBeingComposed(attrs.isDefined(StyleConstants.ComposedTextAttribute));
        }

        if (isPossible(fb.getDocument(), offset, length)) {
            super.replace(fb, offset, length, text, attrs);
        }
    }

    private static final int BIDI_TAG_PADDING = 2;

    boolean isPossible(Document d, int offset, int length) throws BadLocationException {
        // Ensures the method runs in the Swing thread
        UIThreadsUtil.mustBeSwingThread();
        Document3 doc = (Document3) d;

        if (doc.getTrustedChangesInProgress()) {
            return true; // Changes made by internal processes
        }

        if (!doc.isEditMode() || isOffsetOutsideTranslationBounds(doc, offset, length)) {
            return false; // Editing not allowed or offset out of bounds
        }

        if (!Preferences.isPreference(Preferences.ALLOW_TAG_EDITING)) {
            return isEditingAllowedInProtectedParts(doc, offset, length);
        }

        return true;
    }

    private boolean isOffsetOutsideTranslationBounds(Document3 doc, int offset, int length) {
        return offset < doc.getTranslationStart() || offset + length > doc.getTranslationEnd();
    }

    private boolean isEditingAllowedInProtectedParts(Document3 doc, int offset, int length)
            throws BadLocationException {
        SegmentBuilder segmentBuilder = doc.getController().getCurrentSegmentBuilder();
        if (segmentBuilder == null) {
            return false; // No active entry in the document
        }

        String text = doc.getText(doc.getTranslationStart(),
                doc.getTranslationEnd() - doc.getTranslationStart());
        int relativeOffset = offset - doc.getTranslationStart();

        for (ProtectedPart protectedPart : segmentBuilder.ste.getProtectedParts()) {
            if (isOffsetWithinProtectedTag(text, protectedPart, relativeOffset, length, segmentBuilder,
                    doc)) {
                return false; // Editing inside a protected tag
            }
        }

        return true;
    }

    private boolean isOffsetWithinProtectedTag(String text, ProtectedPart protectedPart, int relativeOffset,
            int length, SegmentBuilder segmentBuilder, Document3 doc) {
        String protectedPartText = protectedPart.getTextInSourceSegment();
        if (protectedPartText == null) {
            return false;
        }

        boolean checkRTL = segmentBuilder.hasRTL && doc.getController().targetLangIsRTL;
        int position = -1;

        while ((position = text.indexOf(protectedPartText, position + 1)) >= 0) {
            int checkPos = position;
            int checkLen = protectedPartText.length();

            if (checkRTL && EditorUtils.hasBidiAroundTag(text, protectedPart.getTextInSourceSegment(), position)) {
                checkPos -= BIDI_TAG_PADDING;
                checkLen += BIDI_TAG_PADDING * 2;
            }

            if (isOffsetWithinRange(relativeOffset, length, checkPos, checkLen)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOffsetWithinRange(int offset, int length, int rangeStart, int rangeLength) {
        int rangeEnd = rangeStart + rangeLength;
        return (offset > rangeStart && offset < rangeEnd)
                || (offset + length > rangeStart && offset + length < rangeEnd);
    }
}
