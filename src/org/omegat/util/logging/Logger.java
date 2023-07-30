/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;

public final class Logger {

    private final java.util.logging.Logger logger;

    static {
        boolean loaded = false;
        File usersLogSettings = new File(StaticUtils.getConfigDir(), "logger.properties");

        if (usersLogSettings.isFile() && usersLogSettings.canRead()) {
            // try to load logger settings from user home dir
            try (InputStream in = new FileInputStream(usersLogSettings)) {
                init(in);
                loaded = true;
            } catch (Exception ignored) {
            }
        }
        if (!loaded) {
            // load built-in logger settings
            try (InputStream in = Log.class.getResourceAsStream("/org/omegat/logger.properties")) {
                init(in);
            } catch (IOException ex) {
                System.out.println(OStrings.getString("LOG_FAILED_LOADING_CONFIGURATION"));
                System.out.println(ex.getMessage());
            }
        }
    }

    /**
     * Initialize handlers manually. Required for WebStart.
     *
     * @param in
     *            settings
     */
    private static void init(InputStream in) throws IOException {
        Properties props = new Properties();
        props.load(in);
        String handlers = props.getProperty("handlers");
        if (handlers != null) {
            props.remove("handlers");

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            props.store(b, null);
            LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(b.toByteArray()));

            java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");

            // remove initialized handlers
            for (Handler h : rootLogger.getHandlers()) {
                rootLogger.removeHandler(h);
            }

            String[] hs = handlers.split(",");
            for (String hn : hs) {
                String word = hn.trim();
                try {
                    Class<?> clz = Log.class.getClassLoader().loadClass(word);
                    Handler h = (Handler) clz.getDeclaredConstructor().newInstance();
                    String fname = props.getProperty(word + ".formatter");
                    if (fname != null) {
                        Class<?> clzF = Log.class.getClassLoader().loadClass(fname.trim());
                        h.setFormatter((Formatter) clzF.getDeclaredConstructor().newInstance());
                    }
                    String level = props.getProperty(word + ".level");
                    if (level != null) {
                        h.setLevel(Level.parse(level));
                    }
                    rootLogger.addHandler(h);
                } catch (Exception ex) {
                    System.err.println("Error in logger init: " + ex);
                    ex.printStackTrace();
                }
            }
        }
    }

    private Logger(String name) {
        logger = java.util.logging.Logger.getLogger(name);
    }

    /**
     * Factory method to get logger.
     * 
     * @param name
     *            log name.
     * @return org.omegat.util.logging.Logger object.
     */
    public static org.omegat.util.logging.Logger getLogger(String name) {
        return new Logger(name);
    }

    /**
     * Compute the full path of the log file
     * 
     * @return the full path of the log file
     */
    public String getLogFilePath() {
        return StaticUtils.getLogLocation() + "/" + getLogFileName();
    }

    /**
     * Compute the filename of the log file
     *
     * @return the filename of the log, or an empty string
     */
    public String getLogFileName() {
        for (Handler handler : logger.getParent().getHandlers()) {
            if (handler instanceof OmegaTFileHandler) {
                OmegaTFileHandler omegatLog = (OmegaTFileHandler) handler;
                return omegatLog.getLogFileName();
            }
        }
        return "";
    }

    /**
     * Set a level of logger (backward compatible).
     * 
     * @param level
     *            log level.
     */
    public void setLevel(java.util.logging.Level level) {
        logger.setLevel(level);
    }

    public void log(java.util.logging.Level level, String message, Throwable throwable) {
        logger.log(level, message, throwable);
    }

    public String getName() {
        return logger.getName();
    }

    /**
     * Logs others.
     */
    public void info(String s) {
        logger.info(s);
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
    public void logRB(String key, Object... parameters) {
        if (logger.isLoggable(Level.INFO)) {
            LogRecord rec = new LogRecord(Level.INFO, key);
            rec.setResourceBundle(OStrings.getResourceBundle());
            rec.setParameters(parameters);
            rec.setLoggerName(logger.getName());
            logger.log(rec);
        }
    }

    /**
     * Writes an info message to the log (to be retrieved from the resource
     * bundle).
     * <p>
     * While the info message can be localized, the info key is also logged, so
     * developers can determine what info was given by looking at the error key,
     * instead of trying to interpret localized messages.
     *
     * @param key
     *            The key of the error message in the resource bundle
     * @param parameters
     *            Parameters for the error message.
     */
    public void logInfoRB(String key, Object... parameters) {
        if (logger.isLoggable(Level.INFO)) {
            LogRecord rec = new LogRecord(Level.INFO, key);
            rec.setResourceBundle(OStrings.getResourceBundle());
            rec.setParameters(parameters);
            rec.setLoggerName(logger.getName());
            logger.log(rec);
        }
    }


    /**
     * Writes a throwable message.
     *
     * @param throwable
     *            a throwable to show.
     */
    public void log(Throwable throwable) {
        logger.log(Level.SEVERE, "", throwable);
    }

    /**
     * Writes a debug message through a specified logger (without localization).
     *
     * @param message
     *            message text
     * @param parameters
     *            Parameters for the error message.
     */
    public void logDebug(String message, Object... parameters) {
        if (logger.isLoggable(Level.FINE)) {
            LogRecord rec = new LogRecord(Level.FINE, message);
            rec.setParameters(parameters);
            rec.setLoggerName(logger.getName());
            logger.log(rec);
        }
    }

    /**
     * Writes a warning message.
     *
     * @param key
     *            The key of the error message in the resource bundle
     * @param parameters
     *            Parameters for the error message.
     */
    public void logWarningRB(String key, Object... parameters) {
        if (logger.isLoggable(Level.WARNING)) {
            LogRecord rec = new LogRecord(Level.WARNING, key);
            rec.setResourceBundle(OStrings.getResourceBundle());
            rec.setParameters(parameters);
            rec.setLoggerName(logger.getName());
            logger.log(rec);
        }
    }

    /**
     * Writes an error message through a specified logger.
     *
     * @param key
     *            The key of the error message in the resource bundle
     * @param parameters
     *            Parameters for the error message.
     */
    public void logErrorRB(String key, Object... parameters) {
        if (logger.isLoggable(Level.SEVERE)) {
            LogRecord rec = new LogRecord(Level.SEVERE, key);
            rec.setResourceBundle(OStrings.getResourceBundle());
            rec.setParameters(parameters);
            rec.setLoggerName(logger.getName());
            logger.log(rec);
        }
    }

    /**
     * Writes an error message through a specified logger.
     *
     * @param key
     *            The key of the error message in the resource bundle
     * @param parameters
     *            Parameters for the error message.
     */
    public void logErrorRB(Throwable ex, String key, Object... parameters) {
        if (logger.isLoggable(Level.SEVERE)) {
            LogRecord rec = new LogRecord(Level.SEVERE, key);
            rec.setResourceBundle(OStrings.getResourceBundle());
            rec.setParameters(parameters);
            rec.setLoggerName(logger.getName());
            rec.setThrown(ex);
            logger.log(rec);
        }
    }
}
