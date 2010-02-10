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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.gui.editor;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.editor.mark.CalcMarkersThread;
import org.omegat.gui.editor.mark.IMarker;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.util.Log;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Class for store, manage marks, and controll all markers.
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
    private Highlighter.Highlight[][][] marks;

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
            markerThreads[i] = new CalcMarkersThread(this, m);
            markerThreads[i].start();
        }
    }

    public int getMarkerIndex(final String markerClassName) {
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
        for (CalcMarkersThread th : markerThreads) {
            th.reset();
        }

        highlighter.removeAllHighlights();
        marks = new Highlighter.Highlight[newEntriesCount][][];
        for (int i = 0; i < marks.length; i++) {
            marks[i] = new Highlighter.Highlight[markerNames.length][];
        }
    }

    /**
     * Reset marks for specified entry.
     * 
     * @param entryIndex
     *            entry index
     */
    void resetEntryMarks(int entryIndex) {
        for (int i = 0; i < marks[entryIndex].length; i++) {
            Highlighter.Highlight[] me = marks[entryIndex][i];
            if (me != null) {
                for (int j = 0; j < me.length; j++) {
                    highlighter.removeHighlight(me[j]);
                }
            }
            marks[entryIndex][i] = null;
        }
    }

    public void process(SegmentBuilder[] entryBuilders) {
        for (CalcMarkersThread th : markerThreads) {
            th.add(entryBuilders);
        }
    }

    void setEntryMarks(int entryIndex, SegmentBuilder sb, List<Mark> newMarks,
            int markerIndex) {
        // remove old marks for specified entry and marker
        Highlighter.Highlight[] me = marks[entryIndex][markerIndex];
        if (me != null) {
            for (int j = 0; j < me.length; j++) {
                highlighter.removeHighlight(me[j]);
            }
        }
        marks[entryIndex][markerIndex] = null;

        Highlighter.Highlight[] nm = new Highlighter.Highlight[newMarks.size()];
        int startOffset = sb.getStartPosition() + 1;// skip direction char
        for (int i = 0; i < newMarks.size(); i++) {
            Mark m = newMarks.get(i);
            try {
                nm[i] = (Highlighter.Highlight) highlighter.addHighlight(
                        startOffset + m.startOffset, startOffset + m.endOffset,
                        painters[markerIndex]);
            } catch (BadLocationException ex) {
                Log.log(ex);
            }
        }
        marks[entryIndex][markerIndex] = nm;
    }

    /**
     * Check if entry changed.
     * 
     * @param entryIndex
     * @param sb
     * @param entryVersion
     * @return
     */
    public boolean isEntryChanged(int entryIndex, SegmentBuilder sb,
            long entryVersion) {
        SegmentBuilder ssb;
        try {
            ssb = ec.m_docSegList[entryIndex];
        } catch (Exception e) {
            return true;
        }
        return ssb != sb || ssb.getDisplayVersion() != entryVersion;
    }

    public void markInactiveEntry(final int entryIndex,
            final SegmentBuilder sb, final long entryVersion,
            final List<Mark> marks, final HighlightPainter painter) {
        UIThreadsUtil.mustBeSwingThread();

        if (isEntryChanged(entryIndex, sb, entryVersion)) {
            return;
        }

        // addInactiveEntryMarks(ec.m_docSegList[entryIndex], marks, painter);
    }
}
