/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010-2013 Alex Buloichik
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

package org.omegat.gui.editor.mark;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.omegat.gui.editor.MarkerController;
import org.omegat.gui.editor.SegmentBuilder;
import org.omegat.util.Log;

/**
 * This class calls all marks calculators in background, check if source entry
 * changed, and send add marks to editor. Used for spell/grammar checkers,
 * TransTips, etc.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class CalcMarkersThread extends Thread {

    private final Queue<EntryMarks> forCheck = new LinkedList<EntryMarks>();

    private final MarkerController mController;
    private final int markerIndex;
    public final IMarker marker;

    public CalcMarkersThread(MarkerController mc, IMarker marker, int markerIndex) {
        this.mController = mc;
        this.markerIndex = markerIndex;
        this.marker = marker;
    }

    public void reset() {
        synchronized (forCheck) {
            forCheck.clear();
        }
    }

    public void add(SegmentBuilder[] entryBuilders) {
        List<EntryMarks> vers = new ArrayList<EntryMarks>(entryBuilders.length);

        for (int i = 0; i < entryBuilders.length; i++) {
            EntryMarks v = new EntryMarks(entryBuilders[i], entryBuilders[i].getDisplayVersion(), markerIndex);
            vers.add(v);
        }

        synchronized (forCheck) {
            forCheck.addAll(vers);
            forCheck.notifyAll();
        }
    }

    public void add(SegmentBuilder entryBuilder) {
        EntryMarks v = new EntryMarks(entryBuilder, entryBuilder.getDisplayVersion(), markerIndex);

        synchronized (forCheck) {
            forCheck.add(v);
            forCheck.notifyAll();
        }
    }

    @Override
    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        Thread.currentThread().setName(this.getClass().getSimpleName() + " - " + marker.getClass().getSimpleName());

        try {
            while (true) {
                EntryMarks ev;
                synchronized (forCheck) {
                    ev = forCheck.poll();
                    if (ev == null) {
                        // wait next
                        forCheck.wait();
                    }
                }
                if (ev == null) {
                    continue;
                }

                // Calculate only if entry not changed yet
                try {
                    if (ev.isSegmentChanged()) {
                        // already changed
                        continue;
                    }
                    ev.result = marker.getMarksForEntry(ev.ste, ev.sourceText, ev.translationText, ev.isActive);
                    if (ev.result == null) {
                        // null returned - not need to change anything
                        continue;
                    }
                    if (ev.isSegmentChanged()) {
                        // already changed
                        continue;
                    }
                    mController.queueMarksOutput(ev);
                } catch (Exception ex) {
                    Log.log(ex);
                }
            }
        } catch (InterruptedException ex) {
            Log.log(ex);
        }
    }
}
