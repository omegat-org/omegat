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

package org.omegat.gui.editor.mark;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.SwingUtilities;

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
    private final Queue<EntryMarks> forOutput = new LinkedList<EntryMarks>();

    private final MarkerController mController;
    private final int markerIndex;
    private final IMarker marker;

    public CalcMarkersThread(MarkerController mc, IMarker marker,
            int markerIndex) {
        this.mController = mc;
        this.markerIndex = markerIndex;
        this.marker = marker;
    }

    public void reset() {
        synchronized (forCheck) {
            forCheck.clear();
        }
        synchronized (forOutput) {
            forOutput.clear();
        }
    }

    public void add(SegmentBuilder[] entryBuilders) {
        List<EntryMarks> vers = new ArrayList<EntryMarks>(entryBuilders.length);

        for (int i = 0; i < entryBuilders.length; i++) {
            EntryMarks v = new EntryMarks(i, entryBuilders[i], entryBuilders[i]
                    .getDisplayVersion());
            vers.add(v);
        }

        synchronized (forCheck) {
            forCheck.addAll(vers);
            forCheck.notifyAll();
        }
    }

    public void add(int entryIndex, SegmentBuilder entryBuilder) {
        EntryMarks v = new EntryMarks(entryIndex, entryBuilder, entryBuilder
                .getDisplayVersion());

        synchronized (forCheck) {
            forCheck.add(v);
            forCheck.notifyAll();
        }
    }

    @Override
    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        Thread.currentThread().setName(
                this.getClass().getSimpleName() + " - "
                        + marker.getClass().getSimpleName());

        try {
            while (true) {
                EntryMarks ev;
                synchronized (forCheck) {
                    ev = forCheck.poll();
                    if (ev == null) {
                        // there is no strings in queue - output all
                        showOutput();
                        // wait next
                        forCheck.wait();
                    }
                }
                if (ev == null) {
                    continue;
                }

                // Calculate only if entry not changed yet
                try {
                    if (mController.isEntryChanged(ev)) {
                        continue;
                    }
                    ev.result = marker.getMarksForEntry(ev.sourceText,
                            ev.translationText, ev.isActive);
                    if (mController.isEntryChanged(ev)) {
                        continue;
                    }
                    if (mController.isEntryChanged(ev)) {
                        continue;
                    }
                    synchronized (forOutput) {
                        // output marks
                        forOutput.add(ev);
                    }
                } catch (Exception ex) {
                    Log.log(ex);
                }
            }
        } catch (InterruptedException ex) {
            Log.log(ex);
        }
    }

    /**
     * Show marks in Swing thread.
     */
    protected void showOutput() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                while (true) {
                    EntryMarks ev;
                    synchronized (forOutput) {
                        ev = forOutput.poll();
                    }
                    if (ev == null) {
                        // end of queue
                        return;
                    }
                    if (!mController.isEntryChanged(ev)) {
                        mController.setEntryMarks(ev.entryIndex, ev.builder,
                                ev.result, markerIndex);
                    }
                }
            }
        });
    }
}
