/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2011 Didier Briel
               2016 Didier Briel

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

package org.omegat.filters3.xml.wordpress;

import java.util.regex.Pattern;
import org.omegat.filters3.Attributes;

import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * This class specifies the Wordpress XML Dialect.
 *
 * @author Didier Briel
 */
public class WordpressDialect extends DefaultXMLDialect {
    public static final Pattern WORDPRESS_XMLNS = Pattern
            .compile("xmlns(:\\w+)?=\"http://wordpress.org/export/");

    public WordpressDialect() {

        defineParagraphTags(new String[] {
            "channel",
            "content:encoded",
            "title",
            "description"
        });

        defineIntactTags(new String[] {
           "pubDate",
           "generator",
           "dc:creator",
           "link",
           "guid",
           "title",
           "category"
       });
    }

    /**
     * In the Wordpress filter, all tags starting with wp: should be ignored
     *
     * @param tag
     *            An XML tag
     * @param atts
     *            The attributes associated with the tag
     * @return <code>false</code> if the content of this tag should be
     *         translated, <code>true</code> otherwise
     */
    @Override
    public Boolean validateIntactTag(String tag, Attributes atts) {
        if (tag.startsWith("wp:")) { // All these tags must be ignored
            return true;
        }
        return false;
    }

}
