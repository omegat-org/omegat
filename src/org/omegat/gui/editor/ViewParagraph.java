package org.omegat.gui.editor;

import java.awt.Shape;

import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.Position.Bias;

import org.omegat.gui.editor.OmDocument.OmElementSegPart;

/**
 * Class for use some protected properties.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ViewParagraph extends ParagraphView {
    public ViewParagraph(final Element elem) {
        super(elem);
        OmElementSegPart segPart = (OmElementSegPart) elem.getParentElement();
        setJustification(segPart.isRightAligned() ? StyleConstants.ALIGN_RIGHT
                : StyleConstants.ALIGN_LEFT);
    }

    @Override
    protected int getNextEastWestVisualPositionFrom(int pos, Bias b, Shape a,
            int direction, Bias[] biasRet) throws BadLocationException {
        int r;
        switch (direction) {
        case SwingConstants.WEST:
            r = pos - 1;
            break;
        case SwingConstants.EAST:
            r = pos + 1;
            break;
        default:
            r = pos;
            break;
        }
        return r;
    }
}
