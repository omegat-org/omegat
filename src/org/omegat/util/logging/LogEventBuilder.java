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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

/**
 * Stream style root logger, which accepts JUL style format based on SLF4J.
 * @author Hiroshi Miura
 */
public final class LogEventBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    private String message;
    private String key;
    private Throwable exception;
    private List<Object> arguments;
    private final org.slf4j.event.Level level;
    private final boolean enabled;

    private LogEventBuilder(org.slf4j.event.Level level) {
        enabled = LOGGER.isEnabledForLevel(level);
        this.level = level;
    }

    /**
     * convenience method to return LogMessageBuilder at info level.
     *
     * @return instance.
     */
    public static LogEventBuilder atInfo() {
        return atLevel(org.slf4j.event.Level.INFO);
    }

    /**
     * convenience method to return LogMessageBuilder at warn level.
     *
     * @return instance.
     */
    public static LogEventBuilder atWarn() {
        return atLevel(org.slf4j.event.Level.WARN);
    }

    /**
     * convenience method to return LogMessageBuilder at error level.
     *
     * @return instance.
     */
    public static LogEventBuilder atError() {
        return atLevel(org.slf4j.event.Level.ERROR);
    }

    /**
     * return LogMessageBuilder with a specified log level.
     *
     * @param level SLF4J log level.
     * @return instance.
     */
    public static LogEventBuilder atLevel(org.slf4j.event.Level level) {
        return new LogEventBuilder(level);
    }

    /**
     * accept raw message.
     *
     * @param message text.
     * @return this
     */
    public LogEventBuilder setMessage(String message) {
        this.message = message;
        this.key = null;
        return this;
    }

    /**
     * accept localize message key.
     *
     * @param key key in bundle.
     * @return this
     */
    public LogEventBuilder setLocMessage(String key) {
        this.key = key;
        return this;
    }

    /**
     * accept throwable.
     *
     * @param t throwable object.
     * @return this
     */
    public LogEventBuilder setThrowable(Throwable t) {
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
     * @param arg argument object.
     * @return this
     */
    public LogEventBuilder addArgument(Object arg) {
        getNonNullArguments().add(arg);
        return this;
    }

    /**
     * accept supplier.
     *
     * @param supplier supplier of paramter.
     * @return this
     */
    public LogEventBuilder addArgument(Supplier<?> supplier) {
        if (enabled) {
            addArgument(supplier.get());
        }
        return this;
    }

    /**
     * accept varargs.
     *
     * @param parameters parameters as var args.
     * @return this
     */
    public LogEventBuilder addArguments(Object[] parameters) {
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
