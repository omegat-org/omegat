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

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.Position.Bias;

/**
 * Class for display segmentation marks. It better to paint marks by own
 * component, because we will not have problems with RTL writing in this case.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ViewSegmentMark extends View {

    public ViewSegmentMark(Element elem) {
        super(elem);
    }

    protected String getViewText() {
        return ((OmDocument.OmElementSegmentMark) getElement()).label;
    }

    
    @Override
    public float getAlignment(int axis) {
        if (axis == View.Y_AXIS) {
            OmTextArea c = ((OmTextArea) getContainer());
            JLabel lb = c.getSegmentMarkLabel(getViewText());
            FontMetrics fm = c.getFontMetrics(lb.getFont());
            float h = fm.getHeight();
            float d = fm.getDescent();
            float v = (h - d) / h;
            return v;
        } else {
            return super.getAlignment(axis);
        }
    }

    /**
     * Do not break view by several parts, because we will not be able to align
     * it in RTL presentation.
     */
    @Override
    public int getBreakWeight(int axis, float pos, float len) {
        return BadBreakWeight;
    }

    /**
     * Calculate caret position.
     */
    @Override
    public Shape modelToView(int pos, Shape a, Bias b)
            throws BadLocationException {
        int len = pos - getStartOffset();
        JLabel lb = ((OmTextArea) getContainer())
                .getSegmentMarkLabel(getViewText().substring(0, len));
        Rectangle2D ra = a.getBounds2D();
        Rectangle2D r = a.getBounds2D();
        r.setRect(ra.getX() + lb.getPreferredSize().width, ra.getY(), 1, ra
                .getHeight());
        return r;
    }

    /**
     * Calculate mouse position.
     */
    @Override
    public int viewToModel(float x, float y, Shape a, Bias[] biasReturn) {
        String txt = getViewText();
        JLabel lb = ((OmTextArea) getContainer()).getSegmentMarkLabel("");
        double prevDist = Double.MAX_VALUE;
        double spos = x - a.getBounds2D().getX();
        int i = 0;
        for (; i <= txt.length(); i++) {
            lb.setText(txt.substring(0, i));
            double dist = Math.abs(spos - lb.getPreferredSize().getWidth());
            if (prevDist < dist) {
                return i - 1 + getStartOffset();
            }
            prevDist = dist;
        }
        return getEndOffset();
    }

    @Override
    public float getMinimumSpan(int axis) {
        return getPreferredSpan(axis);
    }

    @Override
    public float getPreferredSpan(int axis) {
        OmTextArea c = (OmTextArea) getContainer();
        JLabel lb = c.getSegmentMarkLabel(getViewText());
        int result;
        if (axis == X_AXIS) {
            result = lb.getPreferredSize().width;
        } else {
            result = lb.getPreferredSize().height;
        }
        return result;
    }

    public void paint(Graphics g, Shape a) {
        OmTextArea c = ((OmTextArea) getContainer());
        JLabel lb = c.getSegmentMarkLabel(getViewText());
        SwingUtilities.paintComponent(g, lb, c, a.getBounds());
    }
}
