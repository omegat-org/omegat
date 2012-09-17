/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.gui.editor;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.editor.mark.CalcMarkersThread;
import org.omegat.gui.editor.mark.EntryMarks;
import org.omegat.gui.editor.mark.IMarker;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.util.Log;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Class for manage marks and controll all markers.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class MarkerController {
    private final EditorController ec;

    /**
     * Array of displayed marks. <br/>
     * 1st dimension - displayed entries,<br/>
     * 2nd dimension - marker,<br/>
     * 3rd dimension - marks
     */
    private MarkInfo[][][] marks;

    /** List of marker's class names. */
    private final String[] markerNames;

    private final HighlightPainter[] painters;

    /** Threads for each marker. */
    protected final CalcMarkersThread[] markerThreads;

    private final Highlighter highlighter;

    MarkerController(EditorController ec) {
        this.ec = ec;
        this.highlighter = ec.editor.getHighlighter();

        List<IMarker> ms = new ArrayList<IMarker>();
        // start all markers threads
        for (Class<?> mc : PluginUtils.getMarkerClasses()) {
            try {
                ms.add((IMarker) mc.newInstance());
            } catch (Exception ex) {
                Log.logErrorRB(ex, "PLUGIN_MARKER_INITIALIZE", mc.getName());
            }
        }
        markerThreads = new CalcMarkersThread[ms.size()];
        markerNames = new String[ms.size()];
        painters = new HighlightPainter[ms.size()];
        for (int i = 0; i < ms.size(); i++) {
            IMarker m = ms.get(i);
            markerNames[i] = m.getClass().getName();
            painters[i] = m.getPainter();
            markerThreads[i] = new CalcMarkersThread(this, m, i);
            markerThreads[i].start();
        }
    }

    /**
     * Get marker's index by class name.
     * 
     * @param markerClassName
     *            marker's class name
     * @return marker's index
     */
    int getMarkerIndex(final String markerClassName) {
        for (int i = 0; i < markerNames.length; i++) {
            if (markerNames[i].equals(markerClassName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Reset all marks for all entries.
     * 
     * @param newEntriesCount
     *            count of newly displayed entries
     */
    void reset(int newEntriesCount) {
        UIThreadsUtil.mustBeSwingThread();

        for (CalcMarkersThread th : markerThreads) {
            th.reset();
        }

        highlighter.removeAllHighlights();
        marks = new MarkInfo[newEntriesCount][][];
        for (int i = 0; i < marks.length; i++) {
            marks[i] = new MarkInfo[markerNames.length][];
        }
    }

    /**
     * Reset marks for specified entry.
     * 
     * @param entryIndex
     *            entry index
     */
    void resetEntryMarks(int entryIndex) {
        MarkInfo[][] m;
        try {
            m = marks[entryIndex];
        } catch (IndexOutOfBoundsException ex) {
            return;
        }
        for (int i = 0; i < m.length; i++) {
            MarkInfo[] me = m[i];
            if (me != null) {
                for (int j = 0; j < me.length; j++) {
                    highlighter.removeHighlight(me[j].underscore);
                }
            }
            marks[entryIndex][i] = null;
        }
    }

    /**
     * Add entries list to processing queue. Used on display new file.
     */
    public void process(SegmentBuilder[] entryBuilders) {
        for (CalcMarkersThread th : markerThreads) {
            th.add(entryBuilders);
        }
    }

    /**
     * Remove all marks for specified marker and add entries list to processing
     * queue for one marker only. Used for recheck displayed file against one
     * marker.
     */
    public void process(SegmentBuilder[] entryBuilders, int markerIndex) {
        for (int i = 0; i < marks.length; i++) {
            MarkInfo[] me = marks[i][markerIndex];
            if (me != null) {
                for (int j = 0; j < me.length; j++) {
                    highlighter.removeHighlight(me[j].underscore);
                }
            }
            marks[i][markerIndex] = null;
        }
        markerThreads[markerIndex].add(entryBuilders);
    }

    /**
     * Add entry to processign queue. Used on one entry changed.
     */
    public void process(int entryIndex, SegmentBuilder entryBuilder) {
        for (CalcMarkersThread th : markerThreads) {
            th.add(entryIndex, entryBuilder);
        }
    }

    /**
     * Return tooltips texts for specified editor position.
     * 
     * @param entryIndex
     * @param pos
     * @return
     */
    public String getToolTips(int entryIndex, int pos) {
        if (entryIndex < 0 || entryIndex >= marks.length) {
            return null;
        }
        if (marks[entryIndex] == null) {
            return null;
        }
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < marks[entryIndex].length; i++) {
            if (marks[entryIndex][i] == null) {
                continue;
            }
            for (MarkInfo t : marks[entryIndex][i]) {
                if (t != null && t.tooltip != null) {
                    if (t.underscore.getStartOffset() <= pos && t.underscore.getEndOffset() >= pos) {
                        if (res.length() > 0) {
                            res.append("<br>");
                        }
                        res.append(t.tooltip);
                    }
                }
            }
        }
        if (res.length() == 0) {
            return null;
        }
        String r = res.toString();
        r = r.replace("<suggestion>", "<b>");
        r = r.replace("</suggestion>", "</b>");
        return "<html>" + r + "</html>";
    }

    /**
     * Set marks for specified entry and marker.
     */
    public void setEntryMarks(int entryIndex, SegmentBuilder sb, List<Mark> newMarks, int markerIndex) {
        // remove old marks for specified entry and marker
        MarkInfo[][] markInfo = marks[entryIndex];
        MarkInfo[] me = markInfo[markerIndex];
        if (me != null) {
            for (int j = 0; j < me.length; j++) {
                highlighter.removeHighlight(me[j].underscore);
            }
        }
        marks[entryIndex][markerIndex] = null;

        if (newMarks == null || newMarks.isEmpty()) {
            // there is no marks
            return;
        }
        MarkInfo[] nm = new MarkInfo[newMarks.size()];
        int sourceStartOffset = sb.getStartSourcePosition();
        int translationStartOffset;
        if (sb.isActive()) {
            translationStartOffset = ec.editor.getOmDocument().getTranslationStart();
        } else {
            translationStartOffset = sb.getStartTranslationPosition();
        }
        for (int i = 0; i < newMarks.size(); i++) {
            Mark m = newMarks.get(i);
            int startOffset = m.entryPart == Mark.ENTRY_PART.SOURCE ? sourceStartOffset
                    : translationStartOffset;
            try {
                nm[i] = new MarkInfo();
                nm[i].underscore = (Highlighter.Highlight) highlighter.addHighlight(startOffset
                        + m.startOffset, startOffset + m.endOffset, painters[markerIndex]);
                nm[i].tooltip = m.toolTipText;
            } catch (BadLocationException ex) {
                Log.log(ex);
            }
        }
        marks[entryIndex][markerIndex] = nm;
    }

    /**
     * Check if entry changed.
     */
    public boolean isEntryChanged(EntryMarks ev) {
        SegmentBuilder ssb;
        try {
            ssb = ec.m_docSegList[ev.entryIndex];
        } catch (Exception e) {
            return true;
        }
        return ssb != ev.builder || ssb.getDisplayVersion() != ev.entryVersion;
    }

    /**
     * Class for store info about displayed mark.
     */
    protected static class MarkInfo {
        Highlighter.Highlight underscore;
        String tooltip;
    }
}
