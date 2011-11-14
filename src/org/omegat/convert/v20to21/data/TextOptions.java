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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.convert.v20to21.data;

import java.io.Serializable;

/**
 * Options for Text filter. Serializable to allow saving to / reading from
 * configuration file.
 * <p>
 * Text filter would have the following options ([+] means default on). Segment
 * text into paragraphs on:
 * <ul>
 * <li>[] Line breaks
 * <li>[+] Empty lines (double line break)
 * <li>[] Never
 * </ul>
 * 
 * 
 * @author Maxym Mykhalchuk
 */
public class TextOptions implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    /**
     * Text filter should segmentOn text into paragraphs on line breaks.
     */
    public static final int SEGMENT_BREAKS = 1;
    /**
     * Defult. Text filter should segmentOn text into paragraphs on empty lines.
     */
    public static final int SEGMENT_EMPTYLINES = 2;
    /**
     * Text filter should not segmentOn text into paragraphs.
     */
    public static final int SEGMENT_NEVER = 3;

    /** Holds value of property. */
    private int segmentOn = SEGMENT_EMPTYLINES;

    /**
     * Returns when Text filter should segmentOn text into paragraphs.
     * 
     * @return One of {@link #SEGMENT_BREAKS}, {@link #SEGMENT_EMPTYLINES},
     *         {@link #SEGMENT_NEVER}.
     */
    public int getSegmentOn() {
        return this.segmentOn;
    }

    /**
     * Sets when Text filter should segmentOn text into paragraphs.
     * 
     * @param segmentOn
     *            One of {@link #SEGMENT_BREAKS}, {@link #SEGMENT_EMPTYLINES},
     *            {@link #SEGMENT_NEVER}.
     */
    public void setSegmentOn(int segmentOn) {
        this.segmentOn = segmentOn;
    }

}
