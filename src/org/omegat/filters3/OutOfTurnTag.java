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

package org.omegat.filters3;

import org.omegat.filters3.xml.Handler;
import org.omegat.filters3.xml.XMLDialect;
import org.omegat.util.StringUtil;

/**
 * Class for collecting out of turn pieces of document.
 *
 * @author Maxym Mykhalchuk
 */
public abstract class OutOfTurnTag extends Tag {
    /** Entry that contains this out of turn tag's content. */
    private Entry entry;

    /** Returns the entry that embodies this out of turn tag. */
    public Entry getEntry() {
        return entry;
    }

    /**
     * Creates a new instance of out of turn tag. This tag wraps around a chunk
     * of text that should be translated separately, not breaking currently
     * collected text.
     */
    public OutOfTurnTag(XMLDialect xmlDialect, Handler handler, String tag, String shortcut,
            Attributes attributes) {
        super(tag, shortcut, Tag.Type.ALONE, attributes);
        entry = new Entry(xmlDialect, handler);
    }

    /**
     * Returns the tag in its original form as it was in original document. Must
     * be implemented by the decendant.
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
    public abstract String toOriginal();

    /**
     * Returns short XML-encoded representation of the out of turn tag to store
     * in TMX, without enclosing &lt;ph&gt;.
     * <p>
     * E.g. for OpenDocument footnote (out of turn tag "text:note-body") <code>
     * &lt;text:note-body>&lt;text:p text:style-name="Endnote"&gt;The endnote
     * appears at the end of the document in OO but in the middle of
     * the segment in OmegaT.&lt;/text:p>&lt;/text:note-body&gt;
     * </code> this method should return the following, if not translated,
     * <code>
     * &amp;lt;text:note-body&amp;gt;&amp;lt;text:p text:style-name="Endnote"&amp;gt;The
     * endnote appears at the end of the document in OO but in the middle of
     * the segment in
     * OmegaT.&amp;lt;/text:p&amp;gt;&amp;lt;/text:note-body&amp;gt;
     * </code>.
     */
    protected String toPartialTMX() {
        StringBuilder buf = new StringBuilder();

        buf.append("&amp;lt;");
        buf.append(getTag());
        buf.append(getAttributes().toString());
        buf.append("&amp;gt;");

        buf.append(StringUtil.makeValidXML(getEntry().translationToOriginal()));

        buf.append("&amp;lt;/");
        buf.append(getTag());
        buf.append("&amp;gt;");

        return buf.toString();
    }

}
