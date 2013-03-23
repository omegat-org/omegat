package org.omegat.gui.editor.mark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.Highlighter.HighlightPainter;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;

/**
 * Abstract marker class that marks source and target text according to some
 * 'pattern' and paints them accoriding to some 'PAINTER' and 'ATTRIBUTES' (all
 * of they defined by implementing classes)
 * 
 * @author Martin Fleurke
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public abstract class AbstractMarker implements IMarker {
    protected HighlightPainter PAINTER;
    protected String toolTip;
    protected AttributeSet ATTRIBUTES;
    protected Pattern pattern;

    public AbstractMarker() throws Exception {
    }

    /**
     * Is the marker enabled?
     * 
     * @return true when enabled (markers are painted). false when disabled (no
     *         markers painted)
     */
    protected abstract boolean isEnabled();

    @Override
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText, boolean isActive)
            throws Exception {

        if (!isEnabled()) {
            return Collections.emptyList();
        }

        List<Mark> r = new ArrayList<Mark>();
        Matcher match;
        if (isActive || Core.getEditor().getSettings().isDisplaySegmentSources() || translationText == null) {// TODO
            initDrawers(true, isActive);
            match = pattern.matcher(sourceText);
            while (match.find()) {
                Mark m = new Mark(Mark.ENTRY_PART.SOURCE, match.start(), match.end());
                m.painter = PAINTER;
                m.toolTipText = toolTip;
                m.attributes = ATTRIBUTES;
                r.add(m);
            }
        }
        if (translationText != null) {
            initDrawers(false, isActive);
            match = pattern.matcher(translationText);
            while (match.find()) {
                Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, match.start(), match.end());
                m.painter = PAINTER;
                m.toolTipText = toolTip;
                m.attributes = ATTRIBUTES;
                r.add(m);
            }
        }

        return r;
    }

    /**
     * Marker can override this method for define different PAINTER, ATTRIBUTES
     * and PATTERN, based on source/translation, active/inactive and other.
     */
    protected void initDrawers(boolean isSource, boolean isActive) {
    }
}
