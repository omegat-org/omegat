/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
           (C) 2009 Didier Briel
 
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

package org.omegat.filters3.xml.typo3;

import java.util.regex.Pattern;

import org.omegat.filters3.Attribute;
import org.omegat.filters3.Attributes;
import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * This class specifies the Typo3 LocManager XML Dialect.
 * 
 * @author Didier Briel
 */
public class Typo3Dialect extends DefaultXMLDialect {
    public static final Pattern TYPO3_ROOT_TAG = Pattern.compile("t3_tt_content");
    public static final Pattern TYPO3_ROOT_TAG2 = Pattern.compile("t3_pages_language_overlay");

    public Typo3Dialect() {

        defineConstraint(CONSTRAINT_ROOT, TYPO3_ROOT_TAG);

        defineParagraphTags(new String[] { "title", "subtitle", "p", "br", "header", "li", "td", "abstract",
                "image_link", "imagecaption", });

        defineIntactTags(new String[] { "l18n_diffsource", });

        // Typo3 requires empty tags to have a closing tag
        // E.g., <title></title> instead of <title/>
        setClosingTagRequired(true);
    }

    /**
     * In the Typo3 LocManager filter, content should be translated in the
     * following condition: The attribute localizable should be = "1"
     * 
     * @param tag
     *            An XML tag
     * @param atts
     *            The attributes associated with the tag
     * @return <code>true</code> if the content of this tag should be
     *         translated, <code>false</code> otherwise
     */
    public Boolean validateTranslatableTag(String tag, Attributes atts) {
        if (atts != null) {
            for (int i = 0; i < atts.size(); i++) {
                Attribute oneAttribute = atts.get(i);
                if (oneAttribute.getName().equalsIgnoreCase("localizable")
                        && oneAttribute.getValue().equalsIgnoreCase("1"))
                    return true;
            }
        }
        return false;
    }
}
