/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2019 Aaron Madlon-Kay
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

package org.omegat.gui.glossary;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.omegat.util.OStrings;
import org.omegat.util.gui.TooltipAttribute;

public class DefaultGlossaryRenderer implements IGlossaryRenderer {

    @Override
    public String getName() {
        return OStrings.getString("GLOSSARY_RENDERER_DEFAULT");
    }

    @Override
    public String getId() {
        return getClass().getCanonicalName();
    }

    @Override
    public void render(GlossaryEntry entry, IRenderTarget<?> trg) {
        trg.append(entry.getSrcText(), SOURCE_ATTRIBUTES);
        trg.append(" = ");

        String[] targets = entry.getLocTerms(false);
        String[] comments = entry.getComments();
        boolean[] priorities = entry.getPriorities();
        String[] origins = entry.getOrigins(false);
        StringBuilder commentsBuf = new StringBuilder();
        for (int i = 0, commentIndex = 0; i < targets.length; i++) {
            if (i > 0 && targets[i].equals(targets[i - 1])) {
                appendCommentsBuf(commentsBuf, commentIndex, comments[i]);
                continue;
            }
            SimpleAttributeSet attrs = new SimpleAttributeSet(TARGET_ATTRIBUTES);
            if (i > 0) {
                trg.append(", ", attrs);
            }
            if (priorities[i]) {
                StyleConstants.setBold(attrs, true);
            }
            attrs.addAttribute(TooltipAttribute.ATTRIBUTE_KEY, new TooltipAttribute(origins[i]));
            trg.append(bracketEntry(targets[i]), attrs);

            commentIndex++;
            appendCommentsBuf(commentsBuf, commentIndex, comments[i]);
        }
        trg.append(commentsBuf.toString(), NOTES_ATTRIBUTES);
    }

    private void appendCommentsBuf(StringBuilder commentsBuf, int commentIndex, String comment) {
        if (!comment.isEmpty()) {
            commentsBuf.append("\n");
            commentsBuf.append(commentIndex);
            commentsBuf.append(". ");
            commentsBuf.append(comment);
        }
    }

    /**
     * If a combined glossary entry contains ',', it needs to be bracketed by quotes, to prevent confusion
     * when entries are combined. However, if the entry contains ';' or '"', it will automatically be
     * bracketed by quotes.
     *
     * @param entry
     *            A glossary text entry
     * @return A glossary text entry possibly bracketed by quotes
     */
    private String bracketEntry(String entry) {
        if (entry.contains(",") && !(entry.contains(";") || entry.contains("\""))) {
            entry = '"' + entry + '"';
        }
        return entry;
    }
}
