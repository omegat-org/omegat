/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2008 Alex Buloichik
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

package org.omegat.util;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * A collection of methods to make logging things easier.
 *
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class Log {

    private Log() {
    }

    private static final org.omegat.util.logging.Logger LOGGER;

    static {
        LOGGER = org.omegat.util.logging.Logger.getLogger("global");
    }

    /**
     * Returns the path to the log file.
     */
    public static String getLogLocation() {
        return StaticUtils.getConfigDir() + "/logs";
    }

    /**
     * Compute the filename of the log file.
     *
     * @return the filename of the log, or an empty string
     */
    public static String getLogFileName() {
        return LOGGER.getLogFileName();
    }

    /**
     * Compute the full path of the log file.
     *
     * @return the full path of the log file
     */
    public static String getLogFilePath() {
        return getLogLocation() + "/" + getLogFileName();
    }

    /**
     * Set the level for the global logger. This is normally determined by the
     * <code>logger.properties</code> file; use this method to override for
     * special use cases.
     *
     * @param level
     *            The new level
     */
    public static void setLevel(Level level) {
        LOGGER.setLevel(level);
    }

    /**
     * Logs what otherwise would go to System.out
     */
    public static void log(String s) {
        LOGGER.info(s);
    }

    /**
     * Logs a message, retrieved from the resource bundle.
     *
     * @param key
     *            The key of the message in the resource bundle.
     * @param parameters
     *            Parameters for the message. These are inserted by using
     *            StaticUtils.format.
     */
    public static void logRB(String key, Object... parameters) {
        LOGGER.logRB(key, parameters);
    }

    /**
     * Logs an Exception or Error.
     *
     * To the log are written: - The class name of the Exception or Error - The
     * message, if any - The stack trace
     *
     * @param throwable
     *            The exception or error to log
     */
    public static void log(Throwable throwable) {
        LOGGER.log(Level.SEVERE, "", throwable);
    }

    /**
     * Writes a warning message to the log (to be retrieved from the resource
     * bundle)
     * <p>
     * While the warning message can be localized, the warning key is also
     * logged, so developers can determine what warning was given by looking at
     * the error key, instead of trying to interpret localized messages.
     *
     * @param key
     *            The key of the error message in the resource bundle
     * @param parameters
     *            Parameters for the error message. These are inserted by using
     *            StaticUtils.format.
     */
    public static void logWarningRB(String key, Object... parameters) {
        LOGGER.logWarningRB(key, parameters);
    }

    /**
     * Writes an info message to the log (to be retrieved from the resource
     * bundle)
     * <p>
     * While the info message can be localized, the info key is also logged, so
     * developers can determine what info was given by looking at the error key,
     * instead of trying to interpret localized messages.
     *
     * @param key
     *            The key of the error message in the resource bundle
     * @param parameters
     *            Parameters for the error message. These are inserted by using
     *            StaticUtils.format.
     */
    public static void logInfoRB(String key, Object... parameters) {
        LOGGER.logInfoRB(key, parameters);
    }

    /**
     * Writes an error message to the log (to be retrieved from the resource
     * bundle)
     * <p>
     * While the error message can be localized, the error key is also logged,
     * so developers can determine what error was given by looking at the error
     * key, instead of trying to interpret localized messages.
     *
     * @param key
     *            The key of the error message in the resource bundle
     * @param parameters
     *            Parameters for the error message. These are inserted by using
     *            StaticUtils.format.
     */
    public static void logErrorRB(String key, Object... parameters) {
        LOGGER.logErrorRB(key, parameters);
    }

    /**
     * Writes an error message to the log (to be retrieved from the resource
     * bundle)
     * <p>
     * While the error message can be localized, the error key is also logged,
     * so developers can determine what error was given by looking at the error
     * key, instead of trying to interpret localized messages.
     *
     * @param ex
     *            The error that was thrown
     * @param key
     *            The key of the error message in the resource bundle
     * @param parameters
     *            Parameters for the error message. These are inserted by using
     *            StaticUtils.format.
     */
    public static void logErrorRB(Throwable ex, String key, Object... parameters) {
        LOGGER.logErrorRB(ex, key, parameters);
    }

    /**
     * Writes debug message to log (without localization)
     *
     * @param message
     *            message text
     * @param parameters
     *            Parameters for the error message. These are inserted by using
     *            StaticUtils.format.
     */
    public static void logDebug(Logger logger, String message, Object... parameters) {
        if (logger.isLoggable(Level.FINE)) {
            LogRecord rec = new LogRecord(Level.FINE, message);
            rec.setParameters(parameters);
            rec.setLoggerName(logger.getName());
            logger.log(rec);
        }
    }
}
