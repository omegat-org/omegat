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
 * A simple, regex-based syntax highlighter for JavaScript.
 * 
 * @author Aaron Madlon-Kay
 */
public class JavaScriptSyntaxHighlighter extends AbstractSyntaxHighlighter {

    public static final String EXTENSION = "js";

    /**
     * Using a list because order of application matters for this implementation
     */
    private static final List<Entry<Pattern, AttributeSet>> PATTERNS;

    static {
        PATTERNS = new ArrayList<>();
        PATTERNS.add(
                new SimpleImmutableEntry<>(
                        // JavaScript keywords from
                        // http://www.w3schools.com/js/js_reserved.asp
                        Pattern.compile("\\b(abstract|arguments|boolean|break|byte|case|catch|char|class|const|"
                                + "continue|debugger|default|delete|do|double|else|enum|eval|export|extends|"
                                + "false|final|finally|float|for|function|goto|if|implements|import|in|"
                                + "instanceof|int|interface|let|long|native|new|null|package|private|protected|"
                                + "public|return|short|static|super|switch|synchronized|this|throw|throws|transient|"
                                + "true|try|typeof|var|void|volatile|while|with|yield)\\b"),
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
