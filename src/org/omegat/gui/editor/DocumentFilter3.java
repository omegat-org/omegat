/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.util.Preferences;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Own implementation of DocumentFilter. It required for disable for user's edit
 * text outside translation.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
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
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        UIThreadsUtil.mustBeSwingThread();
        if (isPossible(fb.getDocument(), offset, 0)) {
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
        UIThreadsUtil.mustBeSwingThread();
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
        if (Preferences.isPreference(Preferences.ALLOW_TAG_EDITING)) {
            // no need to protect
            return false;
        }

        SourceTextEntry ste = doc.controller.getCurrentEntry();
        if (ste == null) {
            // there is no current active entry
            return false;
        }
        // check if inside tag
        String text = doc.getText(doc.getTranslationStart(), doc.getTranslationEnd() - doc.getTranslationStart());
        int off = offset - doc.getTranslationStart();
        for (String tag : ste.getProtectedParts().keySet()) {
            int pos = -1;
            while ((pos = text.indexOf(tag, pos + 1)) >= 0) {
                if (off > pos && off < pos + tag.length()) {
                    return false;
                }
                if (off + length > pos && off + length < pos + tag.length()) {
                    return false;
                }
            }
        }

        return true;
    }
}
