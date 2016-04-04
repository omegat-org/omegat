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

import java.awt.Color;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * An abstract class for supporting syntax highlighting of languages.
 * 
 * @author Aaron Madlon-Kay
 */
public abstract class AbstractSyntaxHighlighter implements IScriptHighlighter {

    static final Color COLOR_ECLIPSE_KEYWORDS = Color.decode("#7F0055");
    static final Color COLOR_ECLIPSE_STRINGS = Color.decode("#2A00FF");
    static final Color COLOR_ECLIPSE_COMMENTS = Color.decode("#3F7F5F");

    static final Pattern PATTERN_DOUBLEQUOTE_STRINGS = Pattern.compile("\".*?(?<!\\\\)(?:\\\\{2})*\"");
    static final Pattern PATTERN_SINGLEQUOTE_STRINGS = Pattern.compile("'.*?(?<!\\\\)(?:\\\\{2})*'");
    static final Pattern PATTERN_SINGLE_LINE_COMMENT = Pattern.compile("//.*$", Pattern.MULTILINE);
    static final Pattern PATTERN_MULTILINE_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

    protected StyledDocument doc;

    @Override
    public void setDocument(StyledDocument document) {
        if (document == null) {
            throw new NullPointerException();
        }
        doc = document;
        doHighlight();
    }

    @Override
    public void doHighlight() {
        String text;
        try {
            text = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            return;
        }
        // Clear current styles
        doc.setCharacterAttributes(0, doc.getLength(), new SimpleAttributeSet(), true);
        for (Entry<Pattern, Color> e : getPatterns()) {
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setForeground(attrs, e.getValue());
            Matcher m = e.getKey().matcher(text);
            while (m.find()) {
                doc.setCharacterAttributes(m.start(), m.end() - m.start(), attrs, true);
            }
        }
    }

    protected abstract Collection<Entry<Pattern, Color>> getPatterns();
}
