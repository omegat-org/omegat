/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2019 Aaron Madlon-Kay
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

package org.omegat.gui.glossary;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.omegat.util.gui.TooltipAttribute;

public class DefaultGlossaryRenderer implements IGlossaryRenderer {

    protected interface IRenderTarget<T> {
        void append(String str);

        void append(String str, AttributeSet attr);

        T get();
    }

    protected static class DocTarget implements IRenderTarget<Void> {
        DocTarget(StyledDocument doc) {
            this.doc = doc;
        }

        private final StyledDocument doc;

        @Override
        public void append(String str) {
            append(str, null);
        }

        @Override
        public void append(String str, AttributeSet attr) {
            try {
                doc.insertString(doc.getLength(), str, attr);
            } catch (BadLocationException e) {
                // Should never happen since we only insert at end
                Logger.getLogger(DefaultGlossaryRenderer.class.getName()).log(Level.SEVERE,
                        e.getLocalizedMessage(), e);
            }
        }

        @Override
        public Void get() {
            return null;
        }
    }

    protected static class HtmlTarget implements IRenderTarget<String> {

        private final StringBuilder buf = new StringBuilder();

        @Override
        public void append(String str) {
            append(str, null);
        }

        @Override
        public void append(String str, AttributeSet attr) {
            if (attr != null) {
                if (StyleConstants.isBold(attr)) {
                    buf.append("<b>");
                }
                if (StyleConstants.isItalic(attr)) {
                    buf.append("<i>");
                }
            }
            buf.append(str);
            if (attr != null) {
                if (StyleConstants.isItalic(attr)) {
                    buf.append("</i>");
                }
                if (StyleConstants.isBold(attr)) {
                    buf.append("</b>");
                }
            }
        }

        @Override
        public String get() {
            return "<html><p>" + buf.toString().replace("\n", "<br>") + "</p></html>";
        }
    }

    @Override
    public void render(GlossaryEntry entry, StyledDocument doc) {
        DocTarget trg = new DocTarget(doc);
        render(entry, trg);
        trg.append("\n\n");
    }

    protected void render(GlossaryEntry entry, IRenderTarget<?> trg) {
        trg.append(entry.getSrcText());
        trg.append(" = ");

        String[] targets = entry.getLocTerms(false);
        String[] comments = entry.getComments();
        boolean[] priorities = entry.getPriorities();
        String[] origins = entry.getOrigins(false);
        StringBuilder commentsBuf = new StringBuilder();
        for (int i = 0, commentIndex = 0; i < targets.length; i++) {
            if (i > 0 && targets[i].equals(targets[i - 1])) {
                if (!comments[i].equals("")) {
                    commentsBuf.append("\n");
                    commentsBuf.append(String.valueOf(commentIndex));
                    commentsBuf.append(". ");
                    commentsBuf.append(comments[i]);
                }
                continue;
            }
            if (i > 0) {
                trg.append(", ");
            }

            SimpleAttributeSet attrs = new SimpleAttributeSet(priorities[i] ? PRIORITY_ATTRIBUTES : NO_ATTRIBUTES);
            attrs.addAttribute(TooltipAttribute.ATTRIBUTE_KEY, new TooltipAttribute(origins[i]));
            trg.append(bracketEntry(targets[i]), attrs);

            commentIndex++;
            if (!comments[i].equals("")) {
                commentsBuf.append("\n");
                commentsBuf.append(String.valueOf(commentIndex));
                commentsBuf.append(". ");
                commentsBuf.append(comments[i]);
            }
        }

        trg.append(commentsBuf.toString());
    }

    public String renderToHtml(GlossaryEntry entry) {
        HtmlTarget trg = new HtmlTarget();
        render(entry, trg);
        return trg.get();
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
