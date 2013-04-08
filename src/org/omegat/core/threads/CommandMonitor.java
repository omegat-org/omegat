/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.omegat.core.Core;

/**
 * Monitor an external process. Inspired by StreamGobbler from:
 * http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4
 * 
 * Used to empty the buffers of external commands so they don't lock up.
 * 
 * @author aaron.madlon-kay
 */
public class CommandMonitor extends Thread {
    
    private static final Logger LOGGER = Logger.getLogger(CommandMonitor.class.getName());
    
    private final InputStream stream;
    private final Process process;
    private final boolean isStdErr;
    private String message = null;
    
    public static CommandMonitor StdoutMonitor(Process process) {
        return new CommandMonitor(process, false);
    }
    
    public static CommandMonitor StderrMonitor(Process process) {
        return new CommandMonitor(process, true);
    }
    
    private CommandMonitor(Process process, boolean isStdErr) {
        this.isStdErr = isStdErr;
        this.process = process;
        this.stream = isStdErr ? process.getErrorStream() : process.getInputStream();
        setName("CommandMonitor");
    }
        
    public void run() {
        InputStreamReader isr = new InputStreamReader(stream);
        BufferedReader br = new BufferedReader(isr);
        
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                if (isStdErr) {
                    LOGGER.warning(line);
                } else {
                    LOGGER.info(line);
                }
                message = line;
            }
        } catch (IOException e) {
            Core.getMainWindow().showStatusMessageRB("CT_ERROR_MONITORING_EXTERNAL_CMD");
        }
        
        int exitValue = -1;
        try {
            exitValue = process.waitFor();
        } catch (Exception e) {
            // Do nothing
        }
        if (isStdErr) {
            if (exitValue > 0 && message == null) {
                Core.getMainWindow().showStatusMessageRB("CT_EXTERNAL_CMD_ERROR", exitValue);
            } else if (exitValue > 0) {
                Core.getMainWindow().showStatusMessageRB("CT_EXTERNAL_CMD_ERROR_MSG", exitValue, message);
            } else if (exitValue < 0) {
                Core.getMainWindow().showStatusMessageRB("CT_EXTERNAL_CMD_INTERRUPTED");
            }
        } else if (exitValue == 0) {
            if (message == null) {
                Core.getMainWindow().showStatusMessageRB("CT_EXTERNAL_CMD_SUCCESS", exitValue);
            } else {
                Core.getMainWindow().showStatusMessageRB("CT_EXTERNAL_CMD_SUCCESS_MSG", exitValue, message);
            }
        }
    }
}
