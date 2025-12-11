/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Didier Briel, Guido Leenders
               2012 Guido Leenders
               2015 Tony Graham
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

package org.omegat.filters3.xml.properties;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.HashMap;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.omegat.filters3.Attribute;
import org.omegat.filters3.Attributes;
import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * This class specifies the Java Properties XML Dialect.
 *
 * @author Tony Graham
 */
@NullMarked
public class PropertiesDialect extends DefaultXMLDialect {
    public static final Pattern PROPERTIES_ROOT_TAG = Pattern.compile("properties");

    /*
     * A map of attribute-name and attribute value pairs that, if exist in a
     * tag, indicate that this tag should not be translated
     */
    private final HashMap<String, Set<String>> ignoreTagsAttributes;

    public PropertiesDialect() {
        defineConstraint(CONSTRAINT_ROOT, PROPERTIES_ROOT_TAG);

        defineParagraphTags(new String[] { "entry", });

        ignoreTagsAttributes = new HashMap<>();

        // Default rules: translate="false|no|0" means do not translate
        addIgnoreAttributeValues("translate", "false", "no", "0");
    }

    private boolean checkIgnoreTags(String key, String value) {
        String k = key.trim().toUpperCase(Locale.ENGLISH);
        String v = value.trim().toUpperCase(Locale.ENGLISH);
        Set<String> values = ignoreTagsAttributes.get(k);
        return values != null && values.contains(v);
    }

    private void addIgnoreAttributeValues(String attributeName, String... values) {
        String key = attributeName.trim().toUpperCase(Locale.ENGLISH);
        Set<String> set = ignoreTagsAttributes.computeIfAbsent(key, k -> new HashSet<>());
        for (String v : values) {
            set.add(v.trim().toUpperCase(Locale.ENGLISH));
        }
    }

    @Override
    public Boolean validateIntactTag(String tag, @Nullable Attributes atts) {
        if (atts != null) {
            for (int i = 0; i < atts.size(); i++) {
                Attribute att = atts.get(i);
                if (checkIgnoreTags(att.getName(), att.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

}
