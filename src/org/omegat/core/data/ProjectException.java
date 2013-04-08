/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Guido Leenders
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.data;

/**
 * ProjectException is a checked exception that may be thrown by validating
 * a project.
 * <p>
 * 
 * @author Guido Leenders
 */
public class ProjectException extends Exception {
    /**
     * Constructs an instance of <code>ProjectException</code> with the
     * specified detail message.
     * 
     * @param msg
     *            the detail message.
     */
    public ProjectException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>ProjectException</code> with the
     * specified detail message and cause.
     * 
     * @param msg
     *            the detail message.
     * @param cause
     *            cause the cause
     */
    public ProjectException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
