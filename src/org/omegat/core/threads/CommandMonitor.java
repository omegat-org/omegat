/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Aaron Madlon-Kay
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
    
    private InputStream stream = null;
    private Process process = null;
    
    public static CommandMonitor StdoutMonitor(Process process) {
        return new CommandMonitor(process.getInputStream(), process);
    }
    
    public static CommandMonitor StderrMonitor(Process process) {
        return new CommandMonitor(process.getErrorStream(), null);
    }
    
    private CommandMonitor(InputStream stream, Process process) {
        this.stream = stream;
        this.process = process;
        setName("CommandMonitor");
    }
        
    public void run() {
        InputStreamReader isr = new InputStreamReader(stream);
        BufferedReader br = new BufferedReader(isr);
        
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                if (process == null) {
                    LOGGER.warning(line);
                } else {
                    LOGGER.info(line);
                }
            }
        } catch (IOException e) {
            Core.getMainWindow().showStatusMessageRB("CT_ERROR_MONITORING_EXTERNAL_CMD");
        }
        
        if (process != null) {
            String value = "?";
            try {
                process.waitFor();
                value = String.format("%s", process.exitValue());
            } catch (Exception e) {
                // Do nothing
            }
            Core.getMainWindow().showStatusMessageRB("CT_EXTERNAL_CMD_DONE", value);
        }
    }
}