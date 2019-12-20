/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
               2016 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: https://omegat.org/support

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;
import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.util.Log;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Class for export current segment and monitor changes for import if external
 * script produce some results.
 *
 * @author Alex Buloichik <alex73mail@gmail.com>
 * @author Aaron Madlon-Kay
 */
public class SegmentExportImport {
    static final int WAIT_TIME = 100;

    private final EditorController controller;
    private volatile long exportLastModified = Long.MAX_VALUE;
    private final File importFile;

    /** The name of the file with the exported selection */
    public static final String SELECTION_EXPORT = "selection.txt";

    /** The name of the file with the target exported segment */
    public static final String TARGET_EXPORT = "target.txt";

    /** The name of the file with the source exported segment */
    public static final String SOURCE_EXPORT = "source.txt";

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

    private static File getFile(String name) {
        return new File(StaticUtils.getScriptDir(), name);
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

        File sourceFile = getFile(SOURCE_EXPORT);
        File targetFile = getFile(TARGET_EXPORT);
        try {
            writeFile(sourceFile, s1);
            writeFile(targetFile, s2);
            exportLastModified = sourceFile.lastModified();
        } catch (IOException ex) {
            Log.log(ex);
        }
    }

    private static void writeFile(File file, String content) throws IOException {
        content = content.replaceAll("\n", System.lineSeparator());
        file.delete();
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }

    public static synchronized void exportCurrentSelection(String selection) {
        try {
            writeFile(getFile(SELECTION_EXPORT), selection);
        } catch (IOException ex) {
            Log.log(ex);
        }
    }

    synchronized void importText() {
        if (importFile.lastModified() < exportLastModified) {
            // check again inside synchronized block
            return;
        }
        exportLastModified = importFile.lastModified() + 1;
        try (FileInputStream fis = new FileInputStream(importFile)) {
            String text = IOUtils.toString(fis, StandardCharsets.UTF_8).replace(System.lineSeparator(),
                    "\n");
            UIThreadsUtil.executeInSwingThread(new Runnable() {
                public void run() {
                    controller.replaceEditText(text);
                }
            });
        } catch (IOException ex) {
            Log.log(ex);
        }
    }

    /**
     * Empties the exported segments.
     */
    public static synchronized void flushExportedSegments() {
        File sourceFile = getFile(SOURCE_EXPORT);
        File targetFile = getFile(TARGET_EXPORT);
        try {
            writeFile(sourceFile, "");
            writeFile(targetFile, "");
        } catch (IOException ex) {
            Log.log(ex);
        }
    }
}
