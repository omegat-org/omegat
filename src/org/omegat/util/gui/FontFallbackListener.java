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

package org.omegat.util.gui;

import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Segment;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class FontFallbackListener implements DocumentListener {
        
    private Font defaultFont;
    
    public FontFallbackListener(final JTextComponent comp) {
        defaultFont = comp.getFont();
        comp.addPropertyChangeListener("font", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getNewValue() != null && !evt.getNewValue().equals(evt.getOldValue())) {
                    defaultFont = (Font) evt.getNewValue();
                    Document doc = comp.getDocument();
                    doStyling(doc, 0, doc.getLength());
                }
            }
        });
    }
    
    @Override
    public void insertUpdate(final DocumentEvent e) {
        doStyling(e.getDocument(), e.getOffset(), e.getLength());
    }
    
    private void doStyling(Document document, final int offset, final int length) {
        if (!(document instanceof StyledDocument)) {
            return;
        }
        
        final StyledDocument doc = (StyledDocument) document;
        
        new SwingWorker<Object, StyleRun>() {
            @Override
            protected Object doInBackground() throws Exception {
                Segment seg = new Segment();
                seg.setPartialReturn(true);
                
                int nleft = length;
                int offs = offset;
                try {
                    while (nleft > 0) {
                        doc.getText(offs, nleft, seg);
                        int i = seg.getBeginIndex();
                        while ((i = defaultFont.canDisplayUpTo(seg, i, seg.getEndIndex())) != -1) {
                            int cp = Character.codePointAt(seg, i - seg.getBeginIndex());
                            int start = i;
                            i += Character.charCount(cp);
                            Font font = FontFallbackManager.getCapableFont(cp);
                            if (font == null) {
                                continue;
                            }
                            // Look ahead to try to group as many characters as possible into this run.
                            for (int cpn, ccn, j = i; j < seg.getEndIndex(); j += ccn) {
                                cpn = Character.codePointAt(seg, j - seg.getBeginIndex());
                                ccn = Character.charCount(cpn);
                                if (!defaultFont.canDisplay(cpn) && font.canDisplay(cpn)) {
                                    i += ccn;
                                } else {
                                    break;
                                }
                            }
                            publish(new StyleRun(start, i - start, getAttributes(font)));
                        }
                        nleft -= seg.count;
                        offs += seg.count;
                    }
                } catch (BadLocationException ex) {
                    // Ignore
                }
                return null;
            }
            
            protected void process(List<StyleRun> chunks) {
                for (StyleRun chunk : chunks) {
                    doc.setCharacterAttributes(chunk.start, chunk.length, chunk.attrs, false);
                }
            };
        }.execute();
    }

    private class StyleRun {
        public final int start;
        public final int length;
        public final AttributeSet attrs;
        
        public StyleRun(int start, int length, AttributeSet attrs) {
            this.start = start;
            this.length = length;
            this.attrs = attrs;
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }
    
    private AttributeSet getAttributes(Font font) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs, font.getFamily());
        StyleConstants.setFontSize(attrs, defaultFont.getSize());
        return attrs;
    }
}
