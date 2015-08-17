/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.gui.editor.mark;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IFontChangedEventListener;
import org.omegat.util.gui.FontFallbackManager;

public class FontFallbackMarker implements IMarker {
    
    private Font editorFont;
    
    public FontFallbackMarker() {
        editorFont = Core.getMainWindow().getApplicationFont();
        CoreEvents.registerFontChangedEventListener(new IFontChangedEventListener() {
            @Override
            public void onFontChanged(Font newFont) {
                editorFont = Core.getMainWindow().getApplicationFont();
                Core.getEditor().remarkOneMarker(FontFallbackMarker.class.getName());
            }
        });
    }

    @Override
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText,
            String translationText, boolean isActive) throws Exception {
        
        if (!isEnabled()) {
            return null;
        }
        
        int srcGlyphMissing;
        if (isActive || Core.getEditor().getSettings().isDisplaySegmentSources() || translationText == null) {
            srcGlyphMissing = editorFont.canDisplayUpTo(sourceText);
        } else {
            srcGlyphMissing = -1;
        }
                
        int trgGlyphMissing = translationText == null ? -1 : editorFont.canDisplayUpTo(translationText);
                
        if (srcGlyphMissing == -1 && trgGlyphMissing == -1) {
            return null;
        }
        List<Mark> marks = new ArrayList<Mark>();
        if (srcGlyphMissing != -1) {
            createMarks(marks, Mark.ENTRY_PART.SOURCE, sourceText, srcGlyphMissing);
        }
                
        if (trgGlyphMissing != -1) {
            createMarks(marks, Mark.ENTRY_PART.TRANSLATION, translationText, trgGlyphMissing);
        }
        
        return marks;
    }
    
    private boolean isEnabled() {
        return Core.getEditor().getSettings().isDoFontFallback();
    }
    
    private void createMarks(List<Mark> acc, Mark.ENTRY_PART part, String text, int firstMissing) {
        char[] chars = text.toCharArray();
        int i = firstMissing;
        while ((i = editorFont.canDisplayUpTo(chars, i, chars.length)) != -1) {
            int cp = Character.codePointAt(chars, i);
            int start = i;
            i += Character.charCount(cp);
            Font font = FontFallbackManager.getCapableFont(cp);
            if (font == null) {
                continue;
            }
            // Look ahead to try to group as many characters as possible into this run.
            for (int cpn, ccn, j = i; j < chars.length; j += ccn) {
                cpn = Character.codePointAt(chars, j);
                ccn = Character.charCount(cpn);
                if (!editorFont.canDisplay(cpn) && font.canDisplay(cpn)) {
                    i += ccn;
                } else {
                    break;
                }
            }
            Mark m = new Mark(part, start, i);
            m.attributes = getAttributes(font);
            acc.add(m);
        }
    }
    
    private AttributeSet getAttributes(Font font) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs, font.getFamily());
        StyleConstants.setFontSize(attrs, editorFont.getSize());
        return attrs;
    }
}
