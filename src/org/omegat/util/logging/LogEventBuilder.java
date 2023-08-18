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

import java.util.function.Supplier;

/**
 * Decorator for SLF4J LoggingEventBulder.
 *
 * @author Hiroshi Miura
 */
public interface LogEventBuilder {

    /**
     * Sets the message of the logging event.
     *
     * @param message
     *            text.
     * @return this
     */
    LogEventBuilder setMessage(String message);

    /**
     * accept a localize message key.
     *
     * @param key
     *            key in a bundle.
     * @return this
     */
    LogEventBuilder setLocMessage(String key);

    /**
     * accept varargs.
     *
     * @param parameters
     *            parameters as var args.
     * @return this
     */
    LogEventBuilder addArguments(Object[] parameters);

    /**
     * Set the cause for the logging event being built.
     * 
     * @param cause
     *            a throwable
     * @return a LogEventBuilder, usually <b>this</b>.
     */
    LogEventBuilder setCause(Throwable cause);

    /**
     * Add an argument to the event being built.
     *
     * @param p
     *            an Object to add.
     * @return a LogEventBuilder, usually <b>this</b>.
     */
    LogEventBuilder addArgument(Object p);

    /**
     * Add an argument supplier to the event being built.
     *
     * @param objectSupplier
     *            an Object supplier to add.
     * @return a LogEventBuilder, usually <b>this</b>.
     */
    LogEventBuilder addArgument(Supplier<?> objectSupplier);

    /**
     * After the logging event is built, performs actual logging.
     * This method must be called for logging to occur.
     */
    void log();
}
