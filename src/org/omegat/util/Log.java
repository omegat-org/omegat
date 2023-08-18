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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.omegat.util.logging.OmegaTFileHandler;

/**
 * A collection of methods to make logging things easier.
 *
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Hiroshi Miura
 */
public final class Log {

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
                LogMessageBuilder.atError().setMessage("Can't open file for logging").addArgument(ex).log();
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

    /**
     * Logs what otherwise would go to System.out
     */
    public static void log(String s) {
        LogMessageBuilder.atInfo().setMessage(s).log();
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
        LogMessageBuilder.atInfo().setLocMessage(key).addArguments(parameters).log();
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
        LogMessageBuilder.atError().setMessage("").setThrowable(throwable).log();
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
        LogMessageBuilder.atWarn().setLocMessage(key).addArguments(parameters).log();
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
        LogMessageBuilder.atInfo().setLocMessage(key).addArguments(parameters).log();
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
        LogMessageBuilder.atError().setLocMessage(key).addArguments(parameters).log();
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
        LogMessageBuilder.atError().setLocMessage(key).addArguments(parameters).setThrowable(ex).log();
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
        LogMessageBuilder.atInfo().setLocMessage(key).addArgument(parameter).log();
    }

    /**
     * Writes an info message to the log (to be retrieved from the resource
     * bundle).
     *
     * @param key
     *            The key of the error message in the resource bundle
     */
    public static void logInfoRB(String key) {
        LogMessageBuilder.atInfo().setLocMessage(key).log();
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
        LogMessageBuilder.atWarn().setLocMessage(key).addArgument(parameter).log();
    }

    /**
     * Writes an error message to the log (to be retrieved from the resource
     * bundle).
     */
    public static void logErrorRB(String key, Object parameter) {
        LogMessageBuilder.atError().setLocMessage(key).addArgument(parameter).log();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    /**
     * Stream style root logger, which accepts JUL style format based on SLF4J.
     */
    public static final class LogMessageBuilder {
        private String message;
        private String key;
        private Throwable exception;
        private List<Object> arguments;
        private final org.slf4j.event.Level level;
        private final boolean enabled;

        private LogMessageBuilder(org.slf4j.event.Level level) {
            enabled = LOGGER.isEnabledForLevel(level);
            this.level = level;
        }

        /**
         * convenience method to return LogMessageBuilder at info level.
         * 
         * @return instance.
         */
        public static LogMessageBuilder atInfo() {
            return atLevel(org.slf4j.event.Level.INFO);
        }

        /**
         * convenience method to return LogMessageBuilder at warn level.
         * 
         * @return instance.
         */
        public static LogMessageBuilder atWarn() {
            return atLevel(org.slf4j.event.Level.WARN);
        }

        /**
         * convenience method to return LogMessageBuilder at error level.
         * 
         * @return instance.
         */
        public static LogMessageBuilder atError() {
            return atLevel(org.slf4j.event.Level.ERROR);
        }

        /**
         * return LogMessageBuilder with a specified log level.
         * 
         * @param level
         *            SLF4J log level.
         * @return instance.
         */
        public static LogMessageBuilder atLevel(org.slf4j.event.Level level) {
            return new LogMessageBuilder(level);
        }

        /**
         * accept raw message.
         * 
         * @param message
         *            text.
         * @return this
         */
        public LogMessageBuilder setMessage(String message) {
            this.message = message;
            this.key = null;
            return this;
        }

        /**
         * accept localize message key.
         * 
         * @param key
         *            key in bundle.
         * @return this
         */
        public LogMessageBuilder setLocMessage(String key) {
            this.key = key;
            return this;
        }

        /**
         * accept throwable.
         * 
         * @param t
         *            throwable object.
         * @return this
         */
        public LogMessageBuilder setThrowable(Throwable t) {
            exception = t;
            return this;
        }

        private List<Object> getNonNullArguments() {
            if (arguments == null) {
                arguments = new ArrayList<>(3);
            }
            return arguments;
        }

        private Object[] getArgumentArray() {
            if (arguments == null) {
                return null;
            }
            return arguments.toArray();
        }

        /**
         * Accept single argument.
         * 
         * @param arg
         *            argument object.
         * @return this
         */
        public LogMessageBuilder addArgument(Object arg) {
            getNonNullArguments().add(arg);
            return this;
        }

        /**
         * accept supplier.
         * 
         * @param supplier
         *            supplier of paramter.
         * @return this
         */
        public LogMessageBuilder addArgument(Supplier<?> supplier) {
            if (enabled) {
                addArgument(supplier.get());
            }
            return this;
        }

        /**
         * accept varargs.
         * 
         * @param parameters
         *            parameters as var args.
         * @return this
         */
        public LogMessageBuilder addArguments(Object[] parameters) {
            if (enabled) {
                getNonNullArguments().addAll(Arrays.asList(parameters));
            }
            return this;
        }

        /**
         * publish log message.
         */
        public void log() {
            if (enabled) {
                if (key != null) {
                    message = OStrings.getString(key);
                }
                if (arguments != null) {
                    message = StringUtil.format(message, getArgumentArray());
                }
                if (key != null) {
                    if (exception == null) {
                        LOGGER.atLevel(level).log(message + " (" + key + ")");
                    } else {
                        LOGGER.atLevel(level).log(message + " (" + key + ")", exception);
                    }
                } else {
                    if (exception == null) {
                        LOGGER.atLevel(level).log(message);
                    } else {
                        LOGGER.atLevel(level).log(message, exception);
                    }
                }
            }
        }
    }
}
