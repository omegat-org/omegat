package org.omegat.gui.editor.mark;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.Highlighter.HighlightPainter;

import org.omegat.core.Core;
import org.omegat.gui.editor.mark.IMarker;
import org.omegat.gui.editor.mark.Mark;

/**
 * Abstract marker class that marks source and target text according to some 'pattern' 
 * and paints them accoriding to some 'PAINTER' (both defined by implementing classes)
 * 
 * @author Martin Fleurke
 */
public abstract class AbstractMarker implements IMarker {
    protected HighlightPainter PAINTER;
    protected String toolTip;
    protected Pattern pattern;

    public AbstractMarker() throws Exception {
    }

    /**
     * returns the painter to use for painting the marks.
     */
    public HighlightPainter getPainter() {
        return PAINTER;
    }

    /**
     * Is the marker enabled?
     * @return true when enabled (markers are painted). false when disabled (no markers painted)
     */
    protected abstract boolean isEnabled();

    public List<Mark> getMarksForEntry(String sourceText, String translationText, boolean isActive)
            throws Exception {

        List<Mark> r = new ArrayList<Mark>();

        if (!isEnabled()) {
            return r;
        }

        Matcher match;
        if (isActive || Core.getEditor().getSettings().isDisplaySegmentSources() || translationText == null) {
            match = pattern.matcher(sourceText);
            while (match.find()) {
                Mark m = new Mark(Mark.ENTRY_PART.SOURCE, match.start(), match.end());
                m.toolTipText = toolTip;
                r.add(m);
            }
        }
        if (translationText != null) {
            match = pattern.matcher(translationText);
            while (match.find()) {
                Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, match.start(), match.end());
                m.toolTipText = toolTip;
                r.add(m);
            }
        }

        return r;
    }
}
