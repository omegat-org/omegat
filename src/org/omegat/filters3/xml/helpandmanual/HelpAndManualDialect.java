/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Didier Briel, Guido Leenders
               2012 Guido Leenders
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

package org.omegat.filters3.xml.helpandmanual;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.omegat.filters3.Attribute;
import org.omegat.filters3.Attributes;
import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * This class specifies the Help&amp;Manual XML Dialect.
 *
 * @author Guido Leenders
 * @author Didier Briel
 */
@NullMarked
public class HelpAndManualDialect extends DefaultXMLDialect {
    public static final Pattern HAM_ROOT_TAG = Pattern.compile("topic|map|helpproject");

    /*
     * A map of attribute-name to a set of attribute values that, if present on a
     * tag, indicate that this tag should not be translated.
     *
     * Keys and values are expected to be case-insensitive. Internally, both are
     * stored in upper-case using the ROOT locale to avoid locale-specific
     * case-folding issues.
     */
    private final Map<String, Set<String>> ignoreTagsAttributes;

    public HelpAndManualDialect() {
        defineConstraint(CONSTRAINT_ROOT, HAM_ROOT_TAG);

        defineParagraphTags(
                new String[] { "caption", "config-value", "variable", "para", "title", "keyword", "li", });

        defineShortcut("link", "li");

        ignoreTagsAttributes = new HashMap<>();

        // Default rules for Help & Manual: translate="false|no|0" means do not translate
        addIgnoreAttributeValues("translate", "false", "no", "0");
    }

    private boolean checkIgnoreTags(@Nullable String key, @Nullable String value) {
        if (key == null || value == null) {
            return false;
        }
        String k = key.trim().toUpperCase(Locale.ENGLISH);
        String v = value.trim().toUpperCase(Locale.ENGLISH);
        Set<String> values = ignoreTagsAttributes.get(k);
        return values != null && values.contains(v);
    }

    private void addIgnoreAttributeValues(@Nullable String attributeName, @Nullable String... values) {
        if (attributeName == null || values == null) {
            return;
        }
        String key = attributeName.trim().toUpperCase(Locale.ENGLISH);
        Set<String> set = ignoreTagsAttributes.computeIfAbsent(key, k -> new HashSet<>());
        for (String v : values) {
            if (v != null) {
                set.add(v.trim().toUpperCase(Locale.ENGLISH));
            }
        }
    }

    /**
     * In the Help&amp;Manual filter, content should be translated in the
     * following condition: The pair attribute-value should not have been
     * declared as untranslatable in the options
     *
     * @param tag
     *            An XML tag
     * @param atts
     *            The attributes associated with the tag
     * @return <code>false</code> if the content of this tag should be
     *         translated, <code>true</code> otherwise
     */
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
