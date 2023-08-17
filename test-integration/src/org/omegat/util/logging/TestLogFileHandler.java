/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 miurahr
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.util.logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.logging.ErrorManager;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import org.omegat.util.OStrings;

/**
 * File handler for test logging.
 *
 * @author Alex Buloichik
 * @author Hiroshi Miura
 */
public class TestLogFileHandler extends StreamHandler {
    private String logFileName;
    private File lockFile;
    private FileOutputStream lockStream;

    public TestLogFileHandler() throws IOException {
        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();

        String level = manager.getProperty(cname + ".level");
        if (level != null) {
            setLevel(Level.parse(level.trim()));
        }

        openFiles(Paths.get(".", "build", "reports", "test-integration").toFile());
    }

   /**
    * @return the name of the current log file
    */
    public String getOmegaTLogFileName(){
       return logFileName;
   }

    /**
     * Open log file and lock.
     */
    @SuppressWarnings("resource")
    private void openFiles(final File dir) throws IOException {
        boolean ignored = dir.mkdirs();
        logFileName = OStrings.getApplicationName();
        lockFile = new File(dir, logFileName + ".log.lck");
        // try to create lock file
        lockStream = new FileOutputStream(lockFile);
        if (lockStream.getChannel().tryLock() != null) {
            setEncoding(StandardCharsets.UTF_8.name());
            setOutputStream(new FileOutputStream(new File(dir, logFileName + ".log"), true));
        }
        setErrorManager(new ErrorManager());
    }

    @Override
    public synchronized void close() throws SecurityException {
        super.close();
        try {
            lockStream.close();
            boolean ignored = lockFile.delete();
        } catch (Exception ex) {
            // shouldn't happen
            ex.printStackTrace();
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
