/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
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

import java.io.File;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.util.FileUtil;
import org.omegat.util.OConsts;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Class for export current segment and monitor changes for import if external
 * script produce some results.
 * 
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public class SegmentExportImport {
    static final int WAIT_TIME = 100;

    private final EditorController controller;
    private volatile long exportLastModified = Long.MAX_VALUE;
    private final File importFile;

    public SegmentExportImport(EditorController controller) {
        this.controller = controller;
        importFile = new File(StaticUtils.getScriptDir(), "import.txt");
        new Thread() {
            public void run() {
                try {
                    while (true) {
                        if (importFile.lastModified() >= exportLastModified) {
                            importText();
                        } else {
                            Thread.sleep(WAIT_TIME);
                        }
                    }
                } catch (InterruptedException ex) {
                }
            }
        }.start();
    }

    /**
     * Export the current source and target segments in text files.
     */
    public synchronized void exportCurrentSegment(final SourceTextEntry ste) {
        importFile.delete();
        if (ste == null) {
            // entry deactivated
            exportLastModified = Long.MAX_VALUE;
            return;
        }

        String s1 = ste.getSrcText();
        TMXEntry te = Core.getProject().getTranslationInfo(ste);
        String s2 = te.isTranslated() ? te.translation : "";

        File sourceFile = FileUtil.writeScriptFile(s1, OConsts.SOURCE_EXPORT);
        FileUtil.writeScriptFile(s2, OConsts.TARGET_EXPORT);
        exportLastModified = sourceFile.lastModified();
    }

    synchronized void importText() {
        if (importFile.lastModified() < exportLastModified) {
            // check again inside synchronized block
            return;
        }
        exportLastModified = importFile.lastModified() + 1;
        final String text = FileUtil.readScriptFile(importFile);
        if (text != null) {
            UIThreadsUtil.executeInSwingThread(new Runnable() {
                public void run() {
                    controller.replaceEditText(text);
                }
            });
        }
    }
}
