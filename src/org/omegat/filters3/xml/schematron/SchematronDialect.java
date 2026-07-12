/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Didier Briel, Guido Leenders
               2012 Guido Leenders
               2015 Tony Graham
               2026 Hiroshi Miura
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

package org.omegat.filters3.xml.schematron;

import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.omegat.filters3.Attribute;
import org.omegat.filters3.Attributes;
import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * This class specifies the Schematron XML Dialect.
 *
 * @author Tony Graham
 */
@NullMarked
public class SchematronDialect extends DefaultXMLDialect {
    private static final Pattern SCHEMATRON_ROOT_TAG = Pattern.compile("schema|pattern");

    /*
     * A map of attribute-name and attribute value pairs that, if exist in a
     * tag, indicate that this tag should not be translated
     */
    private final HashMap<String, String> ignoreTagsAttributes = new HashMap<>();

    public SchematronDialect() {
        defineConstraint(CONSTRAINT_ROOT, SCHEMATRON_ROOT_TAG);
        //defineConstraint(CONSTRAINT_XMLNS, SCHEMATRON_XMLNS);

        defineParagraphTags(new String[] { "assert", "report", });

        defineIntactTags(new String[] { "phase", "active", "ns", "include", "key", "let", });

        ignoreTagsAttributes.put("TRANSLATE", "FALSE");
    }

    private boolean checkIgnoreTags(@Nullable String key, @Nullable String value) {
        if (key == null || value == null) {
            return false;
        }
        String k = key.trim().toUpperCase(Locale.ENGLISH);
        String v = value.trim().toUpperCase(Locale.ENGLISH);
        String values = ignoreTagsAttributes.get(k);
        return values != null && values.equals(v);
    }

    @Override
    public Boolean validateIntactTag(String tag, @Nullable Attributes atts) {
        if (atts != null) {
            // Check configured attribute/value pairs that mark a tag as non-translatable
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
