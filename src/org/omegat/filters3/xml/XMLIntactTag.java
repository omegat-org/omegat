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

import org.omegat.filters3.Tag;

/**
 * XML Tag that surrounds intact portions of XML document.
 *
 * @author Maxym Mykhalchuk
 */
public class XMLIntactTag extends Tag {
    protected XMLIntactEntry intactContents;

    /** Returns the entry to collect text surrounded by intact tag. */
    public XMLIntactEntry getIntactContents() {
        return intactContents;
    }

    /** Creates a new instance of XML Tag */
    public XMLIntactTag(XMLDialect xmlDialect, Handler handler, String tag, String shortcut,
            org.xml.sax.Attributes attributes) {
        super(tag, shortcut, Type.ALONE, XMLUtils.convertAttributes(attributes));
        this.intactContents = new XMLIntactEntry(xmlDialect, handler);
    }

    /** Creates a new instance of XML Tag */
    public XMLIntactTag(XMLDialect xmlDialect, Handler handler, String tag, String shortcut, Type type,
            org.xml.sax.Attributes attributes) {
        super(tag, shortcut, type, XMLUtils.convertAttributes(attributes));
        this.intactContents = new XMLIntactEntry(xmlDialect, handler);
    }

    /**
     * Returns this tag and the intact contents it surrounds. E.g. for
     *
     * <pre>
     * &lt;style&gt;<br>html {<br>&nbsp;&nbsp;&nbsp;background-color: white;<br>}<br>&lt;/style&gt;
     * </pre>
     *
     * should return absolutely the same.
     */
    public String toOriginal() {
        StringBuilder buf = new StringBuilder();

        buf.append("<");
        buf.append(getTag());
        buf.append(getAttributes().toString());
        buf.append(">");

        buf.append(intactContents.sourceToOriginal());

        buf.append("<");
        buf.append("/");
        buf.append(getTag());
        buf.append(">");

        return buf.toString();
    }
}
