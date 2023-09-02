/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2013 Didier Briel
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util.logging;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.ErrorManager;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;

/**
 * File handler for standard logging for support logrotating(on startup only)
 * and file naming with datetime part.
 *
 * This handler will create 'OmegaT.log' for OmegaT(and OmegaT-1.log,
 * OmegaT-2.log for next instances which runned in the same time). logrotate
 * will work only on create handler, i.e. logs from one OmegaT run will be only
 * in one log file. It means, what log file can be much longer than maxinum file
 * size. Maximum file size used only on startup for decide: should we rotate
 * logs or not.
 *
 * Rotated logs will be named like 'OmegaT.20080325.1800.log'.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class OmegaTFileHandler extends StreamHandler {
    private String logFileName;
    private File lockFile;
    private FileOutputStream lockStream;
    private final long maxSize;
    private final int count;

    public OmegaTFileHandler() throws IOException {
        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();

        String level = manager.getProperty(cname + ".level");
        if (level != null) {
            setLevel(Level.parse(level.trim()));
        }

        String maxSizeStr = manager.getProperty(cname + ".size");
        if (maxSizeStr != null) {
            maxSize = Long.parseLong(maxSizeStr);
        } else {
            maxSize = 1024 * 1024;
        }

        String countStr = manager.getProperty(cname + ".count");
        if (countStr != null) {
            count = Integer.parseInt(countStr);
        } else {
            count = 10;
        }

        openFiles(new File(StaticUtils.getConfigDir(), "logs"));
    }

    /**
     * @return the name of the current log file
     */
    public String getLogFileName() {
        return logFileName + ".log";
    }

    /**
     * Open log file and lock.
     */
    @SuppressWarnings("resource")
    private void openFiles(final File dir) throws IOException {
        dir.mkdirs();
        for (int instanceIndex = 0; instanceIndex < 100; instanceIndex++) {
            String fileName = OStrings.getApplicationName()
                    // Instance index
                    + (instanceIndex > 0 ? ("-" + instanceIndex) : "");

            lockFile = new File(dir, fileName + ".log.lck");
            logFileName = fileName;

            // try to create lock file
            lockStream = new FileOutputStream(lockFile);
            if (lockStream.getChannel().tryLock() != null) {
                rotate(dir, fileName);
                setEncoding(StandardCharsets.UTF_8.name());
                setOutputStream(new FileOutputStream(new File(dir, fileName + ".log"), true));
                break;
            }
        }

        setErrorManager(new ErrorManager());
    }

    @Override
    public synchronized void close() throws SecurityException {
        super.close();
        try {
            lockStream.close();
            lockFile.delete();
        } catch (Exception ex) {
            // shouldn't happen
            ex.printStackTrace();
        }
    }

    /**
     * Rotate log files if need.
     */
    private void rotate(final File dir, final String fileName) {
        File logFile = new File(dir, fileName + ".log");
        if (!logFile.exists() || logFile.length() < maxSize) {
            // do not need to rotate
            return;
        }

        String suffix = new SimpleDateFormat("yyyyMMdd.HHmm").format(new Date());
        File destFile = new File(dir, fileName + '.' + suffix + ".log");
        logFile.renameTo(destFile);
        File[] oldLogs = dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().startsWith(fileName + '.') && pathname.getName().endsWith(".log");
            }
        });
        if (oldLogs != null) {
            Arrays.sort(oldLogs, new Comparator<File>() {
                public int compare(File o1, File o2) {
                    return o2.getName().compareToIgnoreCase(o1.getName());
                }
            });
            for (int i = count; i < oldLogs.length; i++) {
                oldLogs[i].delete();
            }
        }
    }

    @Override
    public synchronized void publish(LogRecord record) {
        if (isLoggable(record)) {
            super.publish(record);
            flush();
        }
    }
}
