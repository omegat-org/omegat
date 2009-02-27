package org.omegat.gui.editor;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.Position.Bias;

/**
 * View for represent eod-of-segment.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ViewSegmentsSeparator extends View {
    protected static final int SEPARATOR_HEIGHT = 16;

    public ViewSegmentsSeparator(Element elem) {
        super(elem);
    }

    @Override
    public float getPreferredSpan(int axis) {
        return SEPARATOR_HEIGHT;
    }

    @Override
    public Shape modelToView(int pos, Shape a, Bias b)
            throws BadLocationException {
        Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a
                .getBounds();

        Rectangle lineArea = new Rectangle();

        // fill in the results and return
        lineArea.x = alloc.x;
        lineArea.y = alloc.y;
        lineArea.width = 0;
        lineArea.height = SEPARATOR_HEIGHT;
        return lineArea;
    }

    @Override
    public void paint(Graphics g, Shape allocation) {
    }

    @Override
    public int viewToModel(float x, float y, Shape a, Bias[] biasReturn) {
        return getStartOffset();
    }
}
