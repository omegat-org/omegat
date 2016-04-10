/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.gui.scripting.highlighting;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;

/**
 * A simple, regex-based syntax highlighter for Groovy.
 * 
 * @author Aaron Madlon-Kay
 */
public class GroovySyntaxHighlighter extends AbstractSyntaxHighlighter {
    
    public static final String EXTENSION = "groovy";

    /**
     * Using a list because order of application matters for this implementation
     */
    private static final List<Entry<Pattern, AttributeSet>> PATTERNS;

    static {
        PATTERNS = new ArrayList<>();
        PATTERNS.add(new SimpleImmutableEntry<>(
                // Groovy keywords from
                // http://docs.groovy-lang.org/latest/html/documentation/index.html#_keywords
                Pattern.compile("\\b(as|assert|break|case|catch|class|const|continue|def|default|do|else|"
                        + "enum|extends|false|finally|for|goto|if|implements|import|in|"
                        + "instanceof|interface|new|null|package|return|super|switch|this|"
                        + "throw|throws|trait|true|try|while)\\b"),
                getAttributeSet(COLOR_ECLIPSE_KEYWORDS, true, false)));
        PATTERNS.add(new SimpleImmutableEntry<>(PATTERN_STATIC_VARIABLE,
                getAttributeSet(COLOR_ECLIPSE_STATIC, true, true)));
        PATTERNS.add(new SimpleImmutableEntry<>(PATTERN_DOUBLEQUOTE_STRINGS,
                getAttributeSet(COLOR_ECLIPSE_STRINGS, false, false)));
        PATTERNS.add(new SimpleImmutableEntry<>(PATTERN_SINGLEQUOTE_STRINGS,
                getAttributeSet(COLOR_ECLIPSE_STRINGS, false, false)));
        PATTERNS.add(new SimpleImmutableEntry<>(PATTERN_SINGLE_LINE_COMMENT,
                getAttributeSet(COLOR_ECLIPSE_COMMENTS, false, false)));
        PATTERNS.add(new SimpleImmutableEntry<>(PATTERN_MULTILINE_COMMENT,
                getAttributeSet(COLOR_ECLIPSE_COMMENTS, false, false)));
    }

    @Override
    protected Collection<Entry<Pattern, AttributeSet>> getPatterns() {
        return PATTERNS;
    }
}
