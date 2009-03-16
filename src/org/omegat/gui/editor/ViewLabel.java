/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 - Zoltan Bartko - bartkozoltan@bartkozoltan.com
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

import javax.swing.text.*;

import org.omegat.gui.editor.OmDocument;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

/**
 * Special label view drawing custom colored normal and jagged underlines, as
 * seen on
 * http://forum.java.sun.com/thread.jspa?threadID=5168528&messageID=9647272.
 * 
 * If you want to add new types, add a new unused constant and modify paint()
 * 
 * @author bartkoz
 */
public class ViewLabel extends LabelView {
    /**
     * the element we are wrapping. We need to remember this because LabelView
     * keeps it secret.
     */
    private OmDocument.OmElementText element;

    public ViewLabel(Element elem) {
        super(elem);
        element = (OmDocument.OmElementText) elem;
    }

    /**
     * custom paint the thing. Except for the default it checks for any custom
     * underline prescription and then uses paintLine() or paintJaggedLine()
     */
    public void paint(Graphics g, Shape allocation) {
        super.paint(g, allocation);

        if (element.misspelled != null) {
            for (int i = 0; i < element.misspelled.size(); i++) {
                // 'for' by index much faster than iterator 'for(v:list)'
                OmDocument.MisspelledRegion reg = element.misspelled.get(i);
                int regStart = element.getStartOffset() + reg.off;
                int regEnd = regStart + reg.len;
                try {
                    if (regEnd <= getStartOffset()
                            || regStart >= getEndOffset()) {
                        // region is outside of this view
                        continue;
                    }
                    Shape a = allocation;
                    Rectangle b = (Rectangle) modelToView(regStart, a,
                            Position.Bias.Forward);
                    Rectangle e = (Rectangle) modelToView(regEnd, a,
                            Position.Bias.Forward);
                    Rectangle line = new Rectangle();
                    line.x = b.x;
                    line.y = b.y;
                    line.width = e.x - b.x;
                    line.height = b.height;
                    if (a.intersects(line)) {
                        paintJaggedLine(g, line, Color.red);
                    }
                } catch (BadLocationException ex) {
                }
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

    /**
     * Redefine for read background from parent element.
     */
    @Override
    protected void setPropertiesFromAttributes() {
        super.setPropertiesFromAttributes();

        Element el = element;
        while (el != null) {
            AttributeSet attr = el.getAttributes();
            if (attr.isDefined(StyleConstants.Background)) {
                setBackground(((OmDocument) getDocument()).getBackground(attr));
                break;
            }
            el = el.getParentElement();
        }
    }
}
