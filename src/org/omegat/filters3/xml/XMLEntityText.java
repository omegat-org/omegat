/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.filters3.xml;

import org.omegat.filters3.Text;

/**
 * Internal entity text in XML. Like, for example, in
 * <code>&lt;title&gt;&amp;brandFullName; Credits&lt;/title&gt;</code>.
 * 
 * @author Maxym Mykhalchuk
 */
public class XMLEntityText extends Text {
    private Entity entity;

    /** Creates a piece of XML text. */
    public XMLEntityText(Entity entity) {
        super(entity.getValue());
        this.entity = entity;
    }

    /**
     * Returns the text in its original form as it was in original document.
     * E.g. for <code>Rock&Roll</code> should return <code>Rock&amp;Roll</code>.
     */
    public String toOriginal() {
        return "&" + entity.getName() + ";";
    }

    /**
     * Creates a new instance of XMLText class. Because, well, translating
     * internal entities is, hmm, too complex.
     */
    public Text createInstance(String text) {
        return new XMLText(text, false);
    }
}
