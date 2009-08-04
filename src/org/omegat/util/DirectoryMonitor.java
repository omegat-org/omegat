/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
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

package org.omegat.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Class for monitor directory content changes. It just looks directory every x
 * seconds and run callback if some files changed.
 * 
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public class DirectoryMonitor extends Thread {
    /** Local logger. */
    private static final Logger LOGGER = Logger
            .getLogger(DirectoryMonitor.class.getName());

    private boolean stopped = false;
    protected final File dir;
    protected final Callback callback;
    private final Map<String, FileInfo> existFiles = new TreeMap<String, FileInfo>();
    protected static final long LOOKUP_PERIOD = 1000;

    /**
     * Create monitor.
     * 
     * @param dir
     *            directory to monitoring
     */
    public DirectoryMonitor(final File dir, final Callback callback) {
        this.dir = dir;
        this.callback = callback;
    }

    public File getDir() {
        return dir;
    }

    /**
     * Stop directory monitoring.
     */
    public void fin() {
        stopped = true;
    }

    @Override
    public void run() {
        if (LOOKUP_PERIOD == 0) {
            // don't check
            return;
        }
        setName(this.getClass().getSimpleName());
        setPriority(MIN_PRIORITY);

        while (!stopped) {
            // find deleted or changed files
            for (String fn : new ArrayList<String>(existFiles.keySet())) {
                if (stopped)
                    return;
                File f = new File(fn);
                if (!f.exists()) {
                    // file removed
                    LOGGER.finer("File '" + f + "' removed");
                    existFiles.remove(fn);
                    callback.fileChanged(f);
                } else {
                    FileInfo fi = new FileInfo(f);
                    if (!fi.equals(existFiles.get(fn))) {
                        // file changed
                        LOGGER.finer("File '" + f + "' changed");
                        existFiles.put(fn, fi);
                        callback.fileChanged(f);
                    }
                }
            }

            // find new files
            List<File> foundFiles = new ArrayList<File>();
            readDir(dir, foundFiles);
            for (File f : foundFiles) {
                if (stopped)
                    return;
                String fn = f.getPath();
                if (!existFiles.keySet().contains(fn)) {
                    // file added
                    LOGGER.finer("File '" + f + "' added");
                    existFiles.put(fn, new FileInfo(f));
                    callback.fileChanged(f);
                }
            }
            try {
                Thread.sleep(LOOKUP_PERIOD);
            } catch (InterruptedException ex) {
                stopped = true;
            }
        }
    }

    protected void readDir(final File dir, final List<File> found) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    readDir(f, found);
                } else {
                    found.add(f);
                }
            }
        }
    }

    /**
     * Information about exist file.
     */
    protected class FileInfo {
        public long lastModified, length;

        public FileInfo(final File file) {
            lastModified = file.lastModified();
            length = file.length();
        }

        @Override
        public boolean equals(Object obj) {
            FileInfo o = (FileInfo) obj;
            return lastModified == o.lastModified && length == o.length;
        }
    }

    /**
     * Callback for monitoring.
     */
    public interface Callback {
        /**
         * Called on any file changes - created, modified, deleted.
         */
        void fileChanged(File file);
    }
}
