/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
                         keithgodfrey@users.sourceforge.net
                         907.223.2039

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

package org.omegat.filters2.html2;

import java.io.Serializable;

/**
 * Options for (X)HTML filter.
 * Serializable to allow saving to / reading from configuration file.
 * <p>
 * HTML filter would have the following options
 * ([+] means default on).
 * Add or rewrite encoding declaration in HTML and XHTML files:
 * <ul>
 * <li>[] Always
 * <li>[+] Only if HTML file has a header
 * <li>[]  Only if HTML file has an encoding declaration
 * <li>[] Never
 * </ul>
 *
 * @author Maxym Mykhalchuk
 */
public class HTMLOptions implements Serializable
{
    /** (X)HTML filter should always add/rewrite encoding declaration. */
    public static final int REWRITE_ALWAYS = 1;
    /** Default. (X)HTML filter should rewrite encoding declaration if HTML file has a header. */
    public static final int REWRITE_IFHEADER = 2;
    /** (X)HTML filter should rewrite encoding declaration meta-tag if HTML file has one. */
    public static final int REWRITE_IFMETA = 3;
    /** (X)HTML filter should never rewrite encoding declaration. */
    public static final int REWRITE_NEVER = 4;

    /** Holds value of property. */
    private int rewriteEncoding = REWRITE_IFHEADER;

    /**
     * Returns whether and when (X)HTML filter adds/rewrites encoding declaration.
     * @return One of {@link #REWRITE_ALWAYS}, {@link #REWRITE_IFHEADER}, 
     *                  {@link #REWRITE_IFMETA}, {@link #REWRITE_NEVER}.
     */
    public int getRewriteEncoding()
    {
        return this.rewriteEncoding;
    }

    /**
     * Sets when (X)HTML filter should add/rewrite encoding declaration.
     * @param rewriteEncoding One of {@link #REWRITE_ALWAYS}, {@link #REWRITE_IFHEADER}, 
     *                                  {@link #REWRITE_IFMETA}, {@link #REWRITE_NEVER}.
     */
    public void setRewriteEncoding(int rewriteEncoding)
    {
        this.rewriteEncoding = rewriteEncoding;
    }
}
