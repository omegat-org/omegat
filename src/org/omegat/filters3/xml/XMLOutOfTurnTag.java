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

import org.omegat.filters3.OutOfTurnTag;

/**
 * Out of turn XML Tag.
 *
 * @author Maxym Mykhalchuk
 */
public class XMLOutOfTurnTag extends OutOfTurnTag {
    /** Creates a new instance of XML Tag */
    public XMLOutOfTurnTag(XMLDialect xmlDialect, Handler handler, String tag, String shortcut,
            org.xml.sax.Attributes attributes) {
        super(xmlDialect, handler, tag, shortcut, XMLUtils.convertAttributes(attributes));
    }

    /**
     * Returns the tag in its original form as it was in original document.
     * <p>
     * E.g. for OpenDocument footnote (out of turn tag "text:note-body") <code>
     * &lt;text:note-body&gt;&lt;text:p text:style-name="Endnote"&gt;The endnote
     * appears at the end of the document in OO but in the middle of
     * the segment in OmegaT.&lt;/text:p&gt;&lt;/text:note-body&gt;
     * </code> this method should return the same if not translated, namely
     * <code>
     * &lt;text:note-body&gt;&lt;text:p text:style-name="Endnote"&gt;The endnote
     * appears at the end of the document in OO but in the middle of
     * the segment in OmegaT.&lt;/text:p&gt;&lt;/text:note-body&gt;
     * </code>.
     */
    public String toOriginal() {
        StringBuilder buf = new StringBuilder();

        buf.append("<");
        buf.append(getTag());
        buf.append(getAttributes().toString());
        buf.append(">");

        buf.append(getEntry().translationToOriginal());

        buf.append("</");
        buf.append(getTag());
        buf.append(">");

        return buf.toString();
    }

}
