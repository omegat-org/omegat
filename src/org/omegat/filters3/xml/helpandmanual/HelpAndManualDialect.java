/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Didier Briel, Guido Leenders
               2012 Guido Leenders
 
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

package org.omegat.filters3.xml.helpandmanual;

import java.util.regex.Pattern;
import java.util.HashMap;

import org.omegat.filters3.Attribute;
import org.omegat.filters3.Attributes;
import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * This class specifies the Help&Manual XML Dialect.
 * 
 * @author Guido Leenders
 * @author Didier Briel
 */
public class HelpAndManualDialect extends DefaultXMLDialect {
    public static final Pattern HAM_ROOT_TAG = Pattern.compile("topic|map|helpproject");

    /*
     * A map of attribute-name and attribute value pairs that, if exist in a
     * tag, indicate that this tag should not be translated
     */
    private HashMap<String, String> ignoreTagsAttributes;

    public HelpAndManualDialect() {
        defineConstraint(CONSTRAINT_ROOT, HAM_ROOT_TAG);

        defineParagraphTags(new String[] { "caption", "config-value", "variable", "para", "title", "keyword",
                "li", });

        defineShortcut("link", "li");
        
        ignoreTagsAttributes = new HashMap<String, String>();
        ignoreTagsAttributes.put("TRANSLATE=FALSE", "");
    }

    private boolean checkIgnoreTags(String key, String value) {
        return ignoreTagsAttributes.containsKey(key.toUpperCase() + "=" + value.toUpperCase());
    }

    /**
     * In the Help&Manual filter, content should be translated in the
     * following condition: The pair attribute-value should not have been
     * declared as untranslatable in the options
     *
     * @param tag
     *            An XML tag
     * @param atts
     *            The attributes associated with the tag
     *@return <code>false</code> if the content of this tag should be
     *         translated, <code>true</code> otherwise
     */
    @Override
    public Boolean validateIntactTag(String tag, Attributes atts) {
        if (atts != null) {
            for (int i = 0; i < atts.size(); i++) {
                Attribute oneAttribute = atts.get(i);
                if (checkIgnoreTags(oneAttribute.getName(), oneAttribute.getValue())) {
                    return true;
                }
            }
        }
        // If no key=value pair is found, the tag can be translated
        return false;
    }

}
