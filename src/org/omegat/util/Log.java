/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2008 Alex Buloichik
               2013 Didier Briel
               2023 Hiroshi Miura
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggingEventBuilder;
import tokyo.northside.logging.LoggerDecorator;

import org.omegat.util.logging.OmegaTFileHandler;

/**
 * A collection of methods to make logging things easier.
 *
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Hiroshi Miura
 */
public final class Log {

    private static final Logger LOGGER = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    private Log() {
    }

    static {
        boolean loaded = false;

        String customLogConfig = System.getProperty("java.util.logging.config.file");
        if (customLogConfig != null) {
            File customLogSettings = new File(customLogConfig);
            if (customLogSettings.isFile() && customLogSettings.canRead()) {
                // try to load logger settings from custom file
                try (InputStream in = new FileInputStream(customLogSettings)) {
                    init(in);
                    loaded = true;
                } catch (Exception ignored) {
                }
            }
        }
        if (!loaded) {
            // try to load logger settings from user home dir
            File usersLogSettings = new File(StaticUtils.getConfigDir(), "logger.properties");
            if (usersLogSettings.isFile() && usersLogSettings.canRead()) {
                try (InputStream in = new FileInputStream(usersLogSettings)) {
                    init(in);
                    loaded = true;
                } catch (Exception ignored) {
                }
            }
        }
        if (!loaded) {
            // load built-in logger settings
            try (InputStream in = Log.class.getResourceAsStream("/org/omegat/logger.properties")) {
                init(in);
            } catch (IOException ex) {
                LOGGER.atError().setMessage("Can't open file for logging").setCause(ex).log();
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
        java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
        for (Handler handler : rootLogger.getHandlers()) {
            if (handler instanceof OmegaTFileHandler) {
                OmegaTFileHandler omegatLog = (OmegaTFileHandler) handler;
                return omegatLog.getLogFileName();
            }
        }
        return "";
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
        org.slf4j.ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        Class<? extends org.slf4j.ILoggerFactory> loggerFactoryClass = loggerFactory.getClass();
        String loggerName = loggerFactoryClass.getName();
        if (loggerName.equals("org.slf4j.jul.JDK14LoggerFactory")) {
            // when slf4j-jdk14
            java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
            rootLogger.setLevel(level);
        }
    }

    public static LoggerDecorator deco(final LoggingEventBuilder builder) {
        return LoggerDecorator.deco(builder, OStrings.getResourceBundle());
    }

    /**
     * Logs what otherwise would go to System.out
     */
    public static void log(String s) {
        deco(LOGGER.atInfo()).setMessage(s).log();
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
        deco(LOGGER.atInfo()).logRB(key, parameters);
    }

    /**
     * Logs an Exception or Error. To the log are written: - The class name of
     * the Exception or Error - The message, if any - The stack trace
     *
     * @param throwable
     *            The exception or error to log
     */
    public static void log(Throwable throwable) {
        LOGGER.atError().setMessage("").setCause(throwable).log();
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
        deco(LOGGER.atWarn()).logRB(key, parameters);
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
        deco(LOGGER.atInfo()).log(key, parameters);
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
        deco(LOGGER.atError()).logRB(key, parameters);
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
        deco(LOGGER.atError()).logRB(key, parameters, ex);
    }

    /**
     * Writes a debug message to log (without localization).
     *
     * @param message
     *            message text
     * @param parameters
     *            Parameters for the error message. These are inserted by using
     *            StaticUtils.format.
     */
    @Deprecated
    public static void logDebug(java.util.logging.Logger logger, String message, Object... parameters) {
        if (logger.isLoggable(Level.FINE)) {
            LogRecord rec = new LogRecord(Level.FINE, message);
            rec.setParameters(parameters);
            rec.setLoggerName(logger.getName());
            logger.log(rec);
        }
    }

    /**
     * Writes an info message to the log (to be retrieved from the resource
     * bundle).
     *
     * @param key
     *            The key of the error message in the resource bundle
     * @param parameter
     *            Parameter for the error message.
     */
    public static void logInfoRB(String key, Object parameter) {
        deco(LOGGER.atInfo()).setMessageRB(key).addArgument(parameter).log();
    }

    /**
     * Writes an info message to the log (to be retrieved from the resource
     * bundle).
     *
     * @param key
     *            The key of the error message in the resource bundle
     */
    public static void logInfoRB(String key) {
        deco(LOGGER.atInfo()).setMessageRB(key).log();
    }

    /**
     * Writes a warning message to the log (to be retrieved from the resource
     * bundle).
     *
     * @param key
     *            The key of the error message in the resource bundle
     * @param parameter
     *            Parameter for the error message.
     */
    public static void logWarningRB(String key, Object parameter) {
        deco(LOGGER.atWarn()).setMessageRB(key).addArgument(parameter).log();
    }

    /**
     * Writes an error message to the log (to be retrieved from the resource
     * bundle).
     */
    public static void logErrorRB(String key, Object parameter) {
        deco(LOGGER.atError()).setMessageRB(key).addArgument(parameter).log();
    }
}
