/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.filters2;

/**
 * TranslationException is a checked exception that may be thrown by filter
 * while parsing/writing out the file.
 * <p>
 * Note that a filter may also throw IOException in case of any I/O errors.
 * 
 * @author Maxym Mykhalchuk
 */
public class TranslationException extends Exception {
    /**
     * Constructs an instance of <code>TranslationException</code> with the
     * specified detail message.
     * 
     * @param msg
     *            the detail message.
     */
    public TranslationException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>TranslationException</code> with the
     * specified detail message and cause.
     * 
     * @param msg
     *            the detail message.
     * @param cause
     *            cause the cause
     */
    public TranslationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
