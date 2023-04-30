/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.filters3.xml;

/**
 * A comment in XML file. For example,
 * <code>&lt;!-- here goes the comment --&gt;</code>.
 *
 * @author Maxym Mykhalchuk
 */
public class Comment extends XMLPseudoTag {
    private String comment;

    /** Creates a new instance of Comment */
    public Comment(String comment) {
        this.comment = comment;
    }

    /**
     * Returns the comment in its original form as it was in original document.
     */
    public String toOriginal() {
        return "<!--" + comment + "-->";
    }
}
