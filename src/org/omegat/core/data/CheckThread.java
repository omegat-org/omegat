/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008      Alex Buloichik
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

package org.omegat.core.data;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Class for check thread locking. Should be disabled for production use, but
 * useful for debugging. You can run "telnet 1122" or open
 * "http://localhost:1122" in browser for receive threads dump into log.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class CheckThread extends Thread {
    /** Local logger. */
    private static final Logger LOGGER = Logger.getLogger(CheckThread.class
            .getName());

    @Override
    public void run() {
        try {
            ServerSocket serv = new ServerSocket(1122);
            while (true) {
                Socket s = serv.accept();
                s.close();
                ThreadMXBean mx = ManagementFactory.getThreadMXBean();
                long[] ids = mx.findMonitorDeadlockedThreads();
                if (ids != null) {
                    for (long id : ids) {
                        LOGGER.severe("Deadlocked " + id);
                    }
                }
                for (ThreadInfo ti : mx.getThreadInfo(mx.getAllThreadIds(), 8)) {
                    LOGGER.severe("Thread " + ti.getThreadId() + "("
                            + ti.getThreadName() + ") state="
                            + ti.getThreadState());
                    for (StackTraceElement st : ti.getStackTrace()) {
                        LOGGER.severe("    " + st);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
