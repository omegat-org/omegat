/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2008 Alex Buloichik
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
  * A collection of methods to make logging things easier.
  *
  * @author Henry Pijffers (henry.pijffers@saxnot.com)
  * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class Log {
    
    private static Logger LOGGER;
    
    static {
        LOGGER = Logger.global;

        boolean loaded = false;
        File usersLogSettings = new File(StaticUtils.getConfigDir(),
                "logger.properties");
        if (usersLogSettings.exists()) {
            // try to load logger settings from user home dir
            try {
                InputStream in = new FileInputStream(usersLogSettings);
                try {
                    LogManager.getLogManager().readConfiguration(in);
                    loaded = true;
                } finally {
                    in.close();
                }
            } catch (Exception e) {
            }
        }
        if (!loaded) {
            // load built-in logger settings
            try {
                InputStream in = Log.class
                        .getResourceAsStream("/org/omegat/logger.properties");
                try {
                    LogManager.getLogManager().readConfiguration(in);
                } finally {
                    in.close();
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Can't open file for logging", ex);
            }
        }
    }
    
    /**
     * Returns the path to the log file.
     */
   public static String getLogLocation() {
       return StaticUtils.getConfigDir() + "/logs";
   }
    
    /**
     * Logs what otherwise would go to System.out
     */
    public static void log(String s)
    {
        LOGGER.info(s);
    }

    /**
      * Logs a message, retrieved from the resource bundle.
      *
      * @param key        The key of the message in the resource bundle.
      * @param parameters Parameters for the message. These are inserted by
      *                   using StaticUtils.format.
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public static void logRB(String key, Object... parameters) {
        if (LOGGER.isLoggable(Level.INFO)) {
            LogRecord rec = new LogRecord(Level.INFO, key);
            rec.setResourceBundle(OStrings.getResourceBundle());
            rec.setParameters(parameters);
            rec.setLoggerName(LOGGER.getName());
            LOGGER.log(rec);
        }
    }

    /**
      * Logs an Exception or Error.
      *
      * To the log are written:
      * - The class name of the Exception or Error
      * - The message, if any
      * - The stack trace
      *
      * @param throwable The exception or error to log
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public static void log(Throwable throwable) {
        LOGGER.log(Level.SEVERE, "", throwable);
    }

    /**
      * Writes a warning message to the log (to be retrieved from the
      * resource bundle), preceded by a line containing the warning ID.
      *
      * While the warning message can be localised, the warning ID should not,
      * so developers can determine what warning was given by looking at the
      * warning ID, instead of trying to interpret localised messages.
      *
      * @param key        The key of the warning message in the resource bundle
      * @param parameters Parameters for the warning message. These are
      *                   inserted by using StaticUtils.format.
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public static void logWarningRB(String key, Object... parameters) {
        if (LOGGER.isLoggable(Level.WARNING)) {
            LogRecord rec = new LogRecord(Level.WARNING, key);
            rec.setResourceBundle(OStrings.getResourceBundle());
            rec.setParameters(parameters);
            rec.setLoggerName(LOGGER.getName());
            LOGGER.log(rec);
        }
    }


    /**
     * Writes the specified info message to the log, preceded by a line
     * containing the info ID.
     *
     * While the info message can be localised, the info ID should not,
     * so developers can determine what info was given by looking at the
     * info ID, instead of trying to interpret localised messages.
     *
     * @param id         An identification string for the info
     * @param parameters Parameters for the info message. These are
     *                   inserted by using StaticUtils.format.
     *
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
     * @author Alex Buloichik (alex73mail@gmail.com)
     */
    public static void logInfoRB(String id, Object... parameters) {
        if (LOGGER.isLoggable(Level.INFO)) {
            LogRecord rec = new LogRecord(Level.INFO, id);
            rec.setResourceBundle(OStrings.getResourceBundle());
            rec.setParameters(parameters);
            rec.setLoggerName(LOGGER.getName());
            LOGGER.log(rec);
        }       
    }
    
    /**
      * Writes an error message to the log (to be retrieved from the
      * resource bundle), preceded by a line containing the error ID.
      *
      * While the error message can be localised, the error ID should not,
      * so developers can determine what error was given by looking at the
      * error ID, instead of trying to interpret localised messages.
      *
      * @param id         The key of the error message in the resource bundle
      * @param parameters Parameters for the error message. These are
      *                   inserted by using StaticUtils.format.
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public static void logErrorRB(String key, Object... parameters) {
        if (LOGGER.isLoggable(Level.SEVERE)) {
            LogRecord rec = new LogRecord(Level.SEVERE, key);
            rec.setResourceBundle(OStrings.getResourceBundle());
            rec.setParameters(parameters);
            rec.setLoggerName(LOGGER.getName());
            LOGGER.log(rec);
        }
    }
}
