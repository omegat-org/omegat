/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2010 Didier Briel
               2013 Alex Buloichik 
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

package org.omegat.filters3.xml.xliff;

import java.util.List;

import org.omegat.filters3.Attribute;
import org.omegat.filters3.Attributes;
import org.omegat.filters3.Element;
import org.omegat.filters3.Tag;
import org.omegat.filters3.xml.DefaultXMLDialect;
import org.omegat.filters3.xml.XMLContentBasedTag;
import org.omegat.util.InlineTagHandler;

/**
 * This class specifies XLIFF XML Dialect.
 * 
 * @author Didier Briel
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class XLIFFDialect extends DefaultXMLDialect {
    public XLIFFDialect() {
        defineParagraphTags(new String[] { "source", "target", });

        defineOutOfTurnTags(new String[] { "sub", });

        defineIntactTags(new String[] { "source", "header", "bin-unit", "prop-group", "count-group",
                "alt-trans", "note",
                // "mrk", only <mrk mtype="protected"> should be an intact tag
                "ph", "bpt", "ept", "it", "context", "seg-source", });

        defineContentBasedTags(new String[] { "bpt", "ept", "it" });
    }

    /**
     * In the XLIFF filter, the tag &lt;mrk&gt; is a preformat tag when the
     * attribute "mtype" contains "seg".
     * 
     * @param tag
     *            An XML tag
     * @param atts
     *            The attributes associated with the tag
     * @return <code>true</code> if this tag should be a preformat tag,
     *         <code>false</code> otherwise
     */
    @Override
    public Boolean validatePreformatTag(String tag, Attributes atts) {
        if (!tag.equalsIgnoreCase("mrk")) // We test only "mrk"
            return false;

        if (atts != null) {
            for (int i = 0; i < atts.size(); i++) {
                Attribute oneAttribute = atts.get(i);
                if (oneAttribute.getName().equalsIgnoreCase("mtype")
                        && oneAttribute.getValue().equalsIgnoreCase("seg"))
                    return true;
            }
        }
        return false;
    }

    /**
     * In the XLKIFF filter, content shouldn't be translated if translate="no"
     * http://docs.oasis-open.org/xliff/v1.2/os/xliff-core.html#translate
     * @param tag
     *            An XML tag
     * @param atts
     *            The attributes associated with the tag
     * @return <code>false</code> if the content of this tag should be
     *         translated, <code>true</code> otherwise
     */
    @Override
    public Boolean validateIntactTag(String tag, Attributes atts) {
        if (!tag.equalsIgnoreCase("group") &&     // Translate can only appear in these tags
            !tag.equalsIgnoreCase("trans-unit") &&
            !tag.equalsIgnoreCase("bin-unit")) {
            return false;
        }

        if (atts != null) {
            for (int i = 0; i < atts.size(); i++) {
                Attribute oneAttribute = atts.get(i);
                if ( oneAttribute.getName().equalsIgnoreCase("translate") &&
                        oneAttribute.getValue().equalsIgnoreCase("no")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String constructShortcuts(List<Element> elements) {
        InlineTagHandler tagHandler = new InlineTagHandler();

        StringBuilder r = new StringBuilder();
        for (Element el : elements) {
            if (el instanceof XMLContentBasedTag) {
                XMLContentBasedTag tag = (XMLContentBasedTag) el;
                String shortcut = null;
                if ("bpt".equals(tag.getTag())) {
                    tagHandler.startBPT(tag.getAttribute("rid"), tag.getAttribute("id"));
                    String tagIndex = tagHandler.endBPT().toString();
                    shortcut = '<' + tag.getShortcut() + tagIndex + '>';
                } else if ("ept".equals(tag.getTag())) {
                    tagHandler.startEPT(tag.getAttribute("rid"), tag.getAttribute("id"));
                    String tagIndex = tagHandler.endEPT().toString();
                    shortcut = "</" + tag.getShortcut() + tagIndex + '>';
                } else if ("it".equals(tag.getTag())) {
                    tagHandler.startIT(tag.getAttribute("pos"));
                    String tagIndex = tagHandler.endIT().toString();
                    if ("end".equals(tagHandler.getCurrentPos())) {
                        shortcut = "</" + tag.getShortcut() + tagIndex + '>';
                    } else {
                        shortcut = "<" + tag.getShortcut() + tagIndex + '>';
                    }
                }
                tag.setShortcut(shortcut);
                r.append(shortcut);
            } else if (el instanceof Tag) {
                Tag tag = (Tag) el;
                tagHandler.startOTHER();
                String tagIndex = tagHandler.endOTHER().toString();
                r.append('<').append(tag.getShortcut()).append(tagIndex).append('>');
            } else {
                r.append(el.toShortcut());
            }
        }
        return r.toString();
    }
}
