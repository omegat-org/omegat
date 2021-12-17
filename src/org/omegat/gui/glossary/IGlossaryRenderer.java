/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2019 Aaron Madlon-Kay, Thomas Cordonnier
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

import java.awt.Color;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleConstants;

import org.omegat.util.gui.Styles;

public interface IGlossaryRenderer {
    AttributeSet NO_ATTRIBUTES = Styles.createAttributeSet(null, null, false, null);
    AttributeSet PRIORITY_ATTRIBUTES = Styles.createAttributeSet(null, null, true, null);
    AttributeSet SOURCE_ATTRIBUTES = Styles.createAttributeSet(
            Styles.EditorColor.COLOR_GLOSSARY_SOURCE.getColor(), null, null, null);
    AttributeSet TARGET_ATTRIBUTES = Styles.createAttributeSet(
            Styles.EditorColor.COLOR_GLOSSARY_TARGET.getColor(), null, null, null);
    AttributeSet NOTES_ATTRIBUTES = Styles.createAttributeSet(
            Styles.EditorColor.COLOR_GLOSSARY_NOTE.getColor(), null, null, null);

    interface IRenderTarget<T> {
        void append(String str);

        void append(String str, AttributeSet attr);

        void appendStartIndent(AttributeSet attr);

        T get();
    }

    class DocTarget implements IRenderTarget<Void> {
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
        public void appendStartIndent(AttributeSet attr) {
            append("\n  ", attr);
        }

        @Override
        public Void get() {
            return null;
        }
    }

    class HtmlTarget implements IRenderTarget<String> {

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
                Color attrColor = StyleConstants.getForeground(attr);
                if (attrColor != Color.black) {
                    String colorString = String.format("%02x%02x%02x",
                            attrColor.getRed(), attrColor.getGreen(), attrColor.getBlue());
                    buf.append("<font color=#" + colorString + ">");
                }
            }
            buf.append(str);
            if (attr != null) {
                Color attrColor = StyleConstants.getForeground(attr);
                if (attrColor != Color.black) {
                    buf.append("</font>");
                }
                if (StyleConstants.isItalic(attr)) {
                    buf.append("</i>");
                }
                if (StyleConstants.isBold(attr)) {
                    buf.append("</b>");
                }
            }
        }

        @Override
        public void appendStartIndent(AttributeSet attr) {
            append("&nbsp;&nbsp;", attr);
        }

        @Override
        public String get() {
            return "<html><p>" + buf.toString().replace("\n", "<br>") + "</p></html>";
        }
    }

    // --------------- Identification methods -----------------

    /** Name to be displayed in the drop box. Can be language-dependent **/
    String getName();

    /** String to be stored in config file. Must be language-independent, and unique **/
    String getId();

    // --------------- Rendering methods -----------------

    void render(GlossaryEntry entry, IRenderTarget<?> trg);

    default void render(GlossaryEntry entry, StyledDocument doc) {
        DocTarget trg = new DocTarget(doc);
        render(entry, trg);
        trg.append("\n\n");
    }

    default String renderToHtml(GlossaryEntry entry) {
        HtmlTarget trg = new HtmlTarget();
        render(entry, trg);
        return trg.get();
    }
}
