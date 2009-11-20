/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 - Zoltan Bartko - bartkozoltan@bartkozoltan.com
               2009 Alex Buloichik
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.LabelView;
import javax.swing.text.Position;
import javax.swing.text.Utilities;

import org.omegat.core.Core;
import org.omegat.util.Log;
import org.omegat.util.Token;

/**
 * Custom implementation of view for display spell check errors.
 * 
 * @author bartkoz
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ViewLabel extends LabelView {

    public ViewLabel(final Element el) {
        super(el);
    }

    @Override
    public void paint(Graphics g, Shape a) {
        super.paint(g, a);

        if (!(getElement().getDocument() instanceof Document3)) {
            // document didn't created yet
            return;
        }

        int spellBegin = getStartOffset();
        int spellEnd = getEndOffset();
        Document3 doc = (Document3) getElement().getDocument();
        int segmentAtLocation = -1;

        // find current segment
        for (int i = 0; i < doc.controller.m_docSegList.length; i++) {
            if (doc.controller.m_docSegList[i]
                    .isInsideSegment((spellBegin + spellEnd) / 2)) {
                segmentAtLocation = i;
                break;
            }
        }
        if (segmentAtLocation < 0) {
            // segment not found
            return;
        }

        // check only required to spell part
        SegmentBuilder seg = doc.controller.m_docSegList[segmentAtLocation];
        spellBegin = Math.max(spellBegin, seg.getStartSpellPosition());
        spellEnd = Math.min(spellEnd, seg.getEndSpellPosition());

        if (spellBegin < spellEnd) {
            // is need spell checking ?
            try {
                /*
                 * Find word boundaries. It required if word splitted to several
                 * lines.
                 */
                JTextComponent c = doc.controller.editor;
                spellBegin = Utilities.getWordStart(c, spellBegin);
                spellEnd = Utilities.getWordEnd(c, spellEnd);

                String text = doc.getText(spellBegin, spellEnd - spellBegin);
                text = EditorUtils.removeDirection(text);

                Token[] words = Core.getTokenizer().tokenizeWordsForSpelling(
                        text);
                for (Token w : words) {
                    /*
                     * Document can merge several 'insert's into one element,
                     * so, direction chars could be added to word.
                     */
                    if (doc.controller.spellCheckerThread.isIncorrect(text
                            .substring(w.getOffset(), w.getOffset()
                                    + w.getLength()))) {
                        int posBegin=-99;
                        int posEnd=-99;
                        try {
                        posBegin = Math.max(spellBegin + w.getOffset(),
                                getStartOffset());
                        posEnd = Math.min(spellBegin + w.getOffset()
                                + w.getLength(), getEndOffset());
                        Rectangle b = modelToView(posBegin, a,
                                Position.Bias.Forward).getBounds();
                        Rectangle e = modelToView(posEnd, a,
                                Position.Bias.Backward).getBounds();
                        Rectangle line = new Rectangle();
                        line.x = b.x;
                        line.y = b.y;
                        line.width = e.x - b.x;
                        line.height = b.height;
                        paintJaggedLine(g, line, Color.red);
                        } catch (Exception ex ) {
                            //TODO: remove
                            Log
                                    .log("Error in ViewLabel: text='" + text
                                            + "' posBegin=" + posBegin
                                            + " posEnd=" + posEnd
                                            + " spellBegin=" + spellBegin
                                            + " spellEnd=" + spellEnd
                                            + " w.off=" + w.getOffset()
                                            + " w.len=" + w.getLength());
                            Log.log(ex);
                        }
                    }
                }
            } catch (BadLocationException ex) {
                // it shouldn't be throwed
                ex.printStackTrace();
            }
        }
    }

    /**
     * paint a jagged underline for misspelled parts
     */
    protected void paintJaggedLine(Graphics g, Shape a, Color color) {
        int y = (int) (a.getBounds().getY() + a.getBounds().getHeight() - 1);
        int x1 = (int) a.getBounds().getX();
        int x2 = (int) (a.getBounds().getX() + a.getBounds().getWidth());

        Color old = g.getColor();
        g.setColor(color);

        int w = 3;
        int h = 2;

        Shape prevClip = g.getClip();
        g.setClip(a);
        for (int i = x1; i <= x2; i += w * 2) {
            g.drawArc(i + 0, y - h, w, h, 0, 180);
            g.drawArc(i + w, y - h, w, h, 180, 181);
        }
        g.setClip(prevClip);

        g.setColor(old);
    }
}
