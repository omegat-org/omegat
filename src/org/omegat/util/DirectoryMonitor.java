/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009-2014 Alex Buloichik
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

package org.omegat.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * Class for monitor directory content changes. It just looks directory every x seconds and run callback if
 * some files changed.
 *
 * @author Alex Buloichik <alex73mail@gmail.com>
 * @author Briac Pilpre
 */
public class DirectoryMonitor extends Thread {
    /** Local logger. */
    private static final Logger LOGGER = Logger.getLogger(DirectoryMonitor.class.getName());

    private boolean stopped = false;
    protected final File dir;
    protected final Callback callback;
    protected final DirectoryCallback directoryCallback;
    private final Map<String, FileInfo> existFiles = new TreeMap<String, FileInfo>();
    protected static final long LOOKUP_PERIOD = 1000;

    /**
     * Create monitor.
     *
     * @param dir
     *            directory to monitoring
     */
    public DirectoryMonitor(final File dir, final Callback callback) {
        if (dir == null) {
            throw new IllegalArgumentException("Dir cannot be null.");
        }
        this.dir = dir;
        this.callback = callback;
        this.directoryCallback = null;
    }

    public DirectoryMonitor(final File dir, final Callback callback, final DirectoryCallback directoryCallback) {
        if (dir == null) {
            throw new IllegalArgumentException("Dir cannot be null.");
        }
        this.dir = dir;
        this.callback = callback;
        // Can't call this(dir, callback) because fields are final.
        this.directoryCallback = directoryCallback;
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
        setName(this.getClass().getSimpleName());
        setPriority(MIN_PRIORITY);

        while (!stopped) {
            checkChanges();
            try {
                Thread.sleep(LOOKUP_PERIOD);
            } catch (InterruptedException ex) {
                stopped = true;
            }
        }
    }

    public synchronized Set<File> getExistFiles() {
        Set<File> result = new TreeSet<File>();
        for (String fn : existFiles.keySet()) {
            result.add(new File(fn));
        }
        return result;
    }

    /**
     * Process changes in directory. This method can be called before thread start for load all files from
     * directory immediately.
     */
    public synchronized void checkChanges() {
        boolean directoryChanged = false;
        // find deleted or changed files
        for (String fn : new ArrayList<String>(existFiles.keySet())) {
            if (stopped) {
                return;
            }
            File f = new File(fn);
            if (!f.exists()) {
                // file removed
                LOGGER.finer("File '" + f + "' removed");
                existFiles.remove(fn);
                callback.fileChanged(f);
                directoryChanged = true;
            } else {
                FileInfo fi = new FileInfo(f);
                if (!fi.equals(existFiles.get(fn))) {
                    // file changed
                    LOGGER.finer("File '" + f + "' changed");
                    existFiles.put(fn, fi);
                    callback.fileChanged(f);
                    directoryChanged = true;
                }
            }
        }

        // find new files
        List<File> foundFiles = FileUtil.findFiles(dir, pathname -> true);

        // Make sure files are in the same order regardless of the platform
        foundFiles.sort((f1, f2) -> f1.toString().toLowerCase().compareTo(f2.toString().toLowerCase()));

        for (File f : foundFiles) {
            if (stopped) {
                return;
            }
            String fn = f.getPath();
            if (!existFiles.keySet().contains(fn)) {
                // file added
                LOGGER.finer("File '" + f + "' added");
                existFiles.put(fn, new FileInfo(f));
                callback.fileChanged(f);
                directoryChanged = true;
            }
        }

        if (directoryCallback != null && directoryChanged) {
            directoryCallback.directoryChanged(dir);
        }
    }

    /**
     * Information about exist file.
     */
    protected static class FileInfo {
        public long lastModified, length;

        public FileInfo(final File file) {
            lastModified = file.lastModified();
            length = file.length();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof FileInfo)) {
                return false;
            }
            FileInfo o = (FileInfo) obj;
            return lastModified == o.lastModified && length == o.length;
        }

        @Override
        public int hashCode() {
            return Objects.hash(lastModified, length);
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

    public interface DirectoryCallback {
        /**
         * Called once for every directory where a file was changed - created, modified, deleted.
         */
        void directoryChanged(File file);
    }
}
