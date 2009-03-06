package org.omegat.gui.editor;

import java.awt.Shape;

import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.FlowView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
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
      //  strategy = new LayoutStrategy(segPart.isLangRTL());
        setJustification(segPart.isRightAligned() ? StyleConstants.ALIGN_RIGHT
                : StyleConstants.ALIGN_LEFT);
    }

    @Override
    public int getNextVisualPositionFrom(int pos, Bias b, Shape a,
            int direction, Bias[] biasRet) throws BadLocationException {
        int r = super.getNextVisualPositionFrom(pos, b, a, direction, biasRet);
        return r;
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

    /**
     * Layout strategy for display begin/end mark correctly, i.e. at the end or
     * begin of line.
     * 
     * @deprecated
     */
    public static class LayoutStrategy extends FlowView.FlowStrategy {
        protected boolean isRTL;

        public LayoutStrategy(final boolean isRTL) {
            this.isRTL = isRTL;
        }

        @Override
        protected int layoutRow(FlowView fv, int rowIndex, int pos) {
            int res = super.layoutRow(fv, rowIndex, pos);

            if (isRTL) {
                // Need to swap "begin" and "end" mark view, because paragraph
                // is RTL
                View row = fv.getView(rowIndex);
                if (row.getViewCount() > 1) {
                    /*
                     * only when more than one view, because nothing to do if
                     * there is no 2 view
                     */
                    int p = -1;
                    // find begin mark
                    for (int i = 0; i < row.getViewCount(); i++) {
                        View v = row.getView(i);
                        if (v instanceof ViewSegmentMark) {
                            if (((ViewSegmentMark) v).isBeginMark()) {
                                p = i;
                                break;
                            }
                        }
                    }
                    if (p >= 0) {
                        // move begin mark to the right
                        int target = row.getViewCount() - 1;
                        if (row.getView(target) instanceof ViewSegmentsSeparator) {
                            target--;
                        }
                        moveView(row, p, target);

                        p = -1;
                        // find end mark
                        for (int i = 0; i < row.getViewCount(); i++) {
                            View v = row.getView(i);
                            if (v instanceof ViewSegmentMark) {
                                if (!((ViewSegmentMark) v).isBeginMark()) {
                                    p = i;
                                    break;
                                }
                            }
                        }
                        if (p >= 0) {
                            // move end mark to the left
                            moveView(row, p, 0);
                        }
                    }
                }
            }

            return res;
        }
    }

    private static void moveView(View parentView, int sourcePos, int targetPos) {
        View[] newList = new View[parentView.getViewCount()];
        for (int i = 0; i < newList.length; i++) {
            newList[i] = parentView.getView(i);
        }
        View v = newList[sourcePos];
        if (targetPos > sourcePos) {
            // move to the end
            System.arraycopy(newList, sourcePos + 1, newList, sourcePos,
                    targetPos - sourcePos);
        } else {
            // move to the begin
            System.arraycopy(newList, targetPos, newList, targetPos + 1,
                    sourcePos - targetPos);
        }
        newList[targetPos] = v;
        parentView.replace(0, newList.length, newList);
    }
}
