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

import org.slf4j.spi.LoggingEventBuilder;

import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

/**
 * Stream style logger decorator implementation, which accepts JUL
 * style format based on SLF4J.
 * @author Hiroshi Miura
 */
public final class LogEventBuilderImpl implements LogEventBuilder{
    private String message;
    private String key;
    private Throwable exception;
    private List<Object> arguments;

    private final LoggingEventBuilder loggingEventBuilder;

    public LogEventBuilderImpl(final LoggingEventBuilder builder) {
        loggingEventBuilder = builder;
    }

    /**
     * accept a raw message.
     *
     * @param message text.
     * @return this
     */
    public LogEventBuilderImpl setMessage(String message) {
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
    public LogEventBuilderImpl setLocMessage(String key) {
        this.key = key;
        return this;
    }

    /**
     * accept throwable.
     *
     * @param cause throwable object.
     * @return this
     */
    public LogEventBuilderImpl setCause(Throwable cause) {
        exception = cause;
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
    public LogEventBuilderImpl addArgument(Object arg) {
        getNonNullArguments().add(arg);
        return this;
    }

    /**
     * accept supplier.
     *
     * @param objectSupplier supplier of paramter.
     * @return this
     */
    public LogEventBuilderImpl addArgument(Supplier<Object> objectSupplier) {
        addArgument(objectSupplier.get());
        return this;
    }

    /**
     * accept varargs.
     *
     * @param parameters parameters as var args.
     * @return this
     */
    public LogEventBuilderImpl addArguments(Object[] parameters) {
        getNonNullArguments().addAll(Arrays.asList(parameters));
        return this;
    }

    /**
     * publish log message.
     */
    public void log() {
        if (key != null) {
            message = StringUtil.format(OStrings.getString(key), getArgumentArray()) + " (" + key + ")";
        } else if (message != null) {
            message = StringUtil.format(message, getArgumentArray());
        } else {
            message = "";
        }
        if (exception == null) {
            loggingEventBuilder.log(message);
        } else {
            loggingEventBuilder.log(message, exception);
        }
    }
}
