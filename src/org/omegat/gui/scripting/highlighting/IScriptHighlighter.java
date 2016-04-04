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

import javax.swing.text.StyledDocument;

import org.omegat.util.Log;

/**
 * An interface for simple highlighting of script content.
 * 
 * @author Aaron Madlon-Kay
 */
public interface IScriptHighlighter {

    /**
     * Dummy highlighter that does nothing. Used by default, or for unsupported
     * languages.
     */
    public static final IScriptHighlighter DUMMY_HIGHLIGHTER = new IScriptHighlighter() {
        @Override
        public void setDocument(StyledDocument document) {
        }
        @Override
        public void doHighlight() {
        }
    };

    /**
     * Get a highlighter for the language represented by the specified file
     * extension (e.g. "js" for JavaScript). If the extension isn't recognized,
     * {@link #DUMMY_HIGHLIGHTER} will be returned.
     * 
     * @param extension
     *            File extension
     * @return
     */
    public static IScriptHighlighter getHighlighter(String extension) {
        switch (extension) {
        case GroovySyntaxHighlighter.EXTENSION:
            return new GroovySyntaxHighlighter();
        case JavaScriptSyntaxHighlighter.EXTENSION:
            return new JavaScriptSyntaxHighlighter();
        }
        Log.log("Syntax highlighting not available for script of type " + extension);
        return DUMMY_HIGHLIGHTER;
    }

    /**
     * Set the document on which the highlighter should work. Must not be null.
     */
    public void setDocument(StyledDocument document);

    /**
     * Update the document's highlight. Will throw {@link NullPointerException}
     * if the document has not yet been set with
     * {@link #setDocument(StyledDocument)}.
     */
    public void doHighlight();
}
