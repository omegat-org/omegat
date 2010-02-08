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
import java.util.List;

import org.jdesktop.swingworker.SwingWorker;
import org.omegat.gui.editor.EditorController;
import org.omegat.gui.editor.Mark;
import org.omegat.gui.editor.SegmentBuilder;

/**
 * This class calls all marks calculators in background, check if source entry
 * changed, and send add marks to editor. Used for spell/grammar checkers,
 * TransTips, etc.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class EntriesProcessingThread extends
        SwingWorker<Object, EntryVersion<List<Mark>>> {
    private final List<EntryVersion<List<Mark>>> items = new ArrayList<EntryVersion<List<Mark>>>();

    public EntriesProcessingThread(EditorController ec, IMarker marker) {

    }

    /**
     * Add item for check. All items should be added BEFORE thread start.
     * 
     * @param segmentIndex
     * @param builder
     * @param segmentVersion
     */
    public void add(int segmentIndex, SegmentBuilder builder,
            long segmentVersion) {
        items.add(new EntryVersion<List<Mark>>(segmentIndex, builder,
                segmentVersion));
    }

    @Override
    protected Void doInBackground() throws Exception {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        Thread.currentThread().setName(this.getClass().getSimpleName());

        for (EntryVersion<List<Mark>> ev : items) {
//            ev.result = processInBackground(ev.builder);
//            if (ev.result.size() > 0) {
//                publish(ev);
//            }
        }

        return null;
    }

    @Override
    protected void process(List<EntryVersion<List<Mark>>> chunks) {
        for (EntryVersion<List<Mark>> ev : chunks) {

        }
    }
}
