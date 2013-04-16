/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010-2013 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.editor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.Position;

import org.omegat.core.Core;
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
 * All markers for inactive segment usually executed in background threads(one
 * thread for one marker class), but markers for active segment executed in UI
 * thread immediately.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class MarkerController {
    private final EditorController ec;

    /** List of marker's class names. */
    private final String[] markerNames;

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
        for (IMarker marker : Core.getMarkers()) {
            ms.add(marker);
        }

        markerThreads = new CalcMarkersThread[ms.size()];
        markerNames = new String[ms.size()];
        for (int i = 0; i < ms.size(); i++) {
            IMarker m = ms.get(i);
            markerNames[i] = m.getClass().getName();
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
     * Remove all marks for all entries.
     * 
     * @param newEntriesCount
     *            count of newly displayed entries
     */
    void removeAll() {
        UIThreadsUtil.mustBeSwingThread();

        for (CalcMarkersThread th : markerThreads) {
            th.reset();
        }

        synchronized (outputQueue) {
            outputQueue.clear();
        }

        highlighter.removeAllHighlights();
    }

    /**
     * Remove marks for one segment for one marker.
     */
    void remove(SegmentBuilder sb, int makerIndex) {
        UIThreadsUtil.mustBeSwingThread();

        if (sb.marks == null) {
            return;
        }

        MarkInfo[] me = sb.marks[makerIndex];
        if (me != null) {
            for (int j = 0; j < me.length; j++) {
                if (me[j] != null && me[j].highlight != null) {
                    highlighter.removeHighlight(me[j].highlight);
                }
            }
            sb.marks[makerIndex] = null;
        }
    }

    /**
     * Reprocess all entries for one marker only. Usually used for spell
     * checking or one marker state changes.
     */
    public void reprocess(SegmentBuilder[] entryBuilders, int markerIndex) {
        UIThreadsUtil.mustBeSwingThread();

        for (SegmentBuilder sb : entryBuilders) {
            remove(sb, markerIndex);
        }
        markerThreads[markerIndex].add(entryBuilders);
    }

    /**
     * Reprocess one entry immediately, in current thread. Usually used for
     * active entry.
     */
    public void reprocessImmediately(SegmentBuilder entryBuilder) {
        UIThreadsUtil.mustBeSwingThread();

        entryBuilder.resetTextAttributes();

        List<EntryMarks> evs = new ArrayList<EntryMarks>();
        for (int i = 0; i < markerNames.length; i++) {
            remove(entryBuilder, i);
            try {
                EntryMarks ev = new EntryMarks(entryBuilder, entryBuilder.getDisplayVersion(), i);
                ev.result = markerThreads[i].marker.getMarksForEntry(ev.ste, ev.sourceText, ev.translationText,
                        ev.isActive);
                if (ev.result != null) {
                    evs.add(ev);
                }
            } catch (Throwable ex) {
                Log.log(ex);
            }
        }
        marksOutput(evs);
    }

    /**
     * Process all segment for all markers.
     */
    public void process(SegmentBuilder[] entryBuilders) {
        UIThreadsUtil.mustBeSwingThread();

        for (CalcMarkersThread th : markerThreads) {
            th.add(entryBuilders);
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
        UIThreadsUtil.mustBeSwingThread();

        MarkInfo[][] m = ec.m_docSegList[entryIndex].marks;
        if (m == null) {
            return null;
        }
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < m.length; i++) {
            if (m[i] == null) {
                continue;
            }
            for (MarkInfo t : m[i]) {
                if (t != null && t.tooltip != null) {
                    if (t.tooltip.p0.getOffset() <= pos && t.tooltip.p1.getOffset() >= pos) {
                        if (res.length() > 0) {
                            res.append("<br>");
                        }
                        res.append(t.tooltip.text);
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

    private final Queue<EntryMarks> outputQueue = new LinkedList<EntryMarks>();

    public void queueMarksOutput(EntryMarks ev) {
        synchronized (outputQueue) {
            // output marks
            outputQueue.add(ev);
            outputQueue.notifyAll();
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                List<EntryMarks> evs = new ArrayList<EntryMarks>();
                synchronized (outputQueue) {
                    while (true) {
                        EntryMarks ev = outputQueue.poll();
                        if (ev == null) {
                            break;
                        }
                        evs.add(ev);
                    }
                }
                marksOutput(evs);
            }
        });
    }

    /**
     * Output marks.
     */
    private void marksOutput(List<EntryMarks> evs) {
        UIThreadsUtil.mustBeSwingThread();

        if (evs.isEmpty()) {
            return;
        }
        Document3 doc = ec.editor.getOmDocument();
        doc.trustedChangesInProgress = true;
        try {
            for (int i = 0; i < evs.size(); i++) {
                EntryMarks ev = evs.get(i);
                if (!ev.isSegmentChanged()) {
                    remove(ev.builder, ev.markerIndex);
                    try {
                        if (ev.builder.marks == null) {
                            ev.builder.marks = new MarkInfo[markerNames.length][];
                        }
                        ev.builder.marks[ev.markerIndex] = new MarkInfo[ev.result.size()];
                        for (int j = 0; j < ev.result.size(); j++) {
                            MarkInfo nm = new MarkInfo(ev.result.get(j), ev.builder, doc, highlighter);
                            ev.builder.marks[ev.markerIndex][j] = nm;
                        }
                    } catch (BadLocationException ex) {
                    }
                }
            }
        } finally {
            doc.trustedChangesInProgress = false;
        }
    }

    /**
     * Class for store info about displayed mark.
     */
    protected static class MarkInfo {
        Highlighter.Highlight highlight;
        Tooltip tooltip;

        public MarkInfo(Mark m, SegmentBuilder sb, Document3 doc, Highlighter highlighter) throws BadLocationException {
            if (m.entryPart == Mark.ENTRY_PART.SOURCE && sb.getSourceText() == null) {
                return;
            }

            int sourceStartOffset = sb.getStartSourcePosition();
            int translationStartOffset;
            if (sb.isActive()) {
                translationStartOffset = doc.getTranslationStart();
            } else {
                translationStartOffset = sb.getStartTranslationPosition();
            }
            int startOffset;
            if (m.entryPart == Mark.ENTRY_PART.SOURCE) {
                startOffset = sourceStartOffset;
            } else {
                startOffset = translationStartOffset;
            }
            if (m.painter != null) {
                highlight = (Highlighter.Highlight) highlighter.addHighlight(startOffset + m.startOffset, startOffset
                        + m.endOffset, m.painter);
            }
            if (m.toolTipText != null) {
                tooltip = new Tooltip(doc, startOffset + m.startOffset, startOffset + m.endOffset, m.toolTipText);
            }
            if (m.attributes != null) {
                doc.setCharacterAttributes(startOffset + m.startOffset, m.endOffset - m.startOffset, m.attributes,
                        false);
            }
        }
    }

    protected static class Tooltip {
        Position p0, p1;
        String text;

        public Tooltip(Document3 doc, int start, int end, String text) throws BadLocationException {
            p0 = doc.createPosition(start);
            p1 = doc.createPosition(end);
            this.text = text;
        }
    }
}
