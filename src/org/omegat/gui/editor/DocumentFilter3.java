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
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {
        UIThreadsUtil.mustBeSwingThread();

        if (attr != null) {
            ((Document3) fb.getDocument()).textBeingComposed = attr
                    .isDefined(StyleConstants.ComposedTextAttribute);
        }

        if (isPossible(fb.getDocument(), offset, 0)) {
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
        UIThreadsUtil.mustBeSwingThread();

        if (attrs != null) {
            ((Document3) fb.getDocument()).textBeingComposed = attrs
                    .isDefined(StyleConstants.ComposedTextAttribute);
        }

        if (isPossible(fb.getDocument(), offset, length)) {
            super.replace(fb, offset, length, text, attrs);
        }
    }

    private boolean isPossible(Document d, int offset, int length) throws BadLocationException {
        Document3 doc = (Document3) d;
        if (doc.trustedChangesInProgress) {
            // this call created by internal changes
            return true;
        }

        if (!doc.isEditMode()) {
            // segment not active - change disabled
            return false;
        }

        if (offset < doc.getTranslationStart() || offset + length > doc.getTranslationEnd()) {
            // Is inside translation ?
            return false;
        }

        // check protected parts
        if (!Preferences.isPreference(Preferences.ALLOW_TAG_EDITING)) {
            SegmentBuilder sb = doc.controller.getCurrentSegmentBuilder();
            if (sb == null) {
                // there is no current active entry
                return false;
            }
            // check if inside tag
            String text = doc.getText(doc.getTranslationStart(), doc.getTranslationEnd() - doc.getTranslationStart());
            int off = offset - doc.getTranslationStart();
            for (ProtectedPart pp : sb.ste.getProtectedParts()) {
                int pos = -1;
                while ((pos = text.indexOf(pp.getTextInSourceSegment(), pos + 1)) >= 0) {
                    int checkPos = pos;
                    int checkLen = pp.getTextInSourceSegment().length();
                    if (sb.hasRTL && doc.controller.targetLangIsRTL) {
                        // should be bidi-chars around tags
                        if (EditorUtils.hasBidiAroundTag(text, pp.getTextInSourceSegment(), pos)) {
                            checkPos -= 2;
                            checkLen += 4;
                        }
                    }
                    if (off > checkPos && off < checkPos + checkLen) {
                        return false;
                    }
                    if (off + length > checkPos && off + length < checkPos + checkLen) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
