/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.data;

/**
 * Tracks usage and frequency of words and word pairs
 *
 * @author Keith Godfrey
 */
public final class StringData {

    private StringData() {
    }

    // uniq flag set indicates that a given token doesn't occur
    // elsewhere, flag clear indicates it has a (at least one) partner
    // near flag means that a given word has different neighbors
    // than in its compared-to string (this a constant used elsewhere)
    public static final byte UNIQ = 0x01;
    public static final byte PAIR = 0x02;
}
