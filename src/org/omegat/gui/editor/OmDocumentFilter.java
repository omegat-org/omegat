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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.gui.editor;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

import org.omegat.util.gui.UIThreadsUtil;

/**
 * Own implementation of DocumentFilter. It required for disable for user's edit
 * text outside translation.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class OmDocumentFilter extends DocumentFilter {
    @Override
    public void remove(FilterBypass fb, int offset, int length)
            throws BadLocationException {
        UIThreadsUtil.mustBeSwingThread();
        if (isInsideTranslation(fb.getDocument(), offset, length)) {
            super.remove(fb, offset, length);
        }
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string,
            AttributeSet attr) throws BadLocationException {
        UIThreadsUtil.mustBeSwingThread();
        if (isInsideTranslation(fb.getDocument(), offset, 0)) {
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text,
            AttributeSet attrs) throws BadLocationException {
        UIThreadsUtil.mustBeSwingThread();
        if (isInsideTranslation(fb.getDocument(), offset, length)) {
            super.replace(fb, offset, length, text, attrs);
        }
    }

    private boolean isInsideTranslation(Document d, int offset, int length) {
        OmDocument doc = (OmDocument) d;
        if (doc.activeTranslationBegin == null
                || doc.activeTranslationEnd == null) {
            // segment not active - change disabled
            return false;
        }

        // Is inside translation ?
        return (offset >= doc.activeTranslationBegin.getOffset()+1 && offset
                + length <= doc.activeTranslationEnd.getOffset()-1);
    }
}
