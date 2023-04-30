/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.filters3;

import org.omegat.util.StringUtil;

/**
 * Abstract piece of text.
 * <p>
 * P.S. The most important method is {@link #createInstance(String)}.
 *
 * @author Maxym Mykhalchuk
 */
public abstract class Text implements Element {
    /** The text itself. */
    private StringBuilder text;

    /** Returns the piece of text stored. */
    public String getText() {
        return text.toString();
    }

    private boolean meaningEvaluated = false;
    private boolean meaningful;

    /** Whether the text is meaningful, i.e. contains anything but space. */
    public boolean isMeaningful() {
        if (!meaningEvaluated) {
            meaningful = !text.toString().trim().isEmpty();
            meaningEvaluated = true;
        }
        return meaningful;
    }

    /** Creates a new instance of Text initialized with some text. */
    public Text(String text) {
        this.text = new StringBuilder(text);
    }

    public void append(String text) {
        this.text.append(text);
    }

    /**
     * Creates a new instance of the same class as this one. This method is used
     * while translating to create pieces of translated text.
     * <p>
     * For example, the following HTML sentence
     * <code>&lt;i&gt;Friday&lt;/i&gt; I'm in love.</code> (Tag[&lt;i&gt;],
     * Text[Friday], Tag[&lt;/i&gt;], Text[ I'm in love.]) can be translated as
     * <code>V &lt;i&gt;pyatnitzu&lt;/i&gt; ya vlyublyon.</code> (Text[V ],
     * Tag[&lt;i&gt;], Text[pyatnitzu], Tag[&lt;/i&gt;], Text[ ya vlyublyon.]).
     * Tags are the same, but text is translated. Even the number of text
     * elements might change! So OmegaT must be able to create Text classes with
     * appropriate {@link #toOriginal()} methods for storing translated text, so
     * it would pick any of the original Text instances, e.g. Text[Friday], and
     * call <code>createInstance("V ")</code>,
     * <code>createInstance("pyatnitzu")</code>, and
     * <code>createInstance(" ya vlyublyon.")</code>.
     */
    public abstract Text createInstance(String text);

    /**
     * Returns shortcut string representation of the element. Basically, the
     * text itself.
     */
    public String toShortcut() {
        return text.toString();
    }

    public String toSafeCalcShortcut() {
        return toShortcut();
    }

    /**
     * Returns long XML-encoded representation of the element. Basically, the
     * XML-encoded text (&lt; -&gt; &amp;lt; etc). E.g. for <code>Rock&amp;Roll</code>
     * should return <code>Rock&amp;Roll</code>.
     */
    public String toTMX() {
        return StringUtil.makeValidXML(text.toString());
    }

    /**
     * Returns the text in its original form as it was in original document.
     * E.g. for <code>Rock&amp;Roll</code> should return <code>Rock&amp;Roll</code>
     * for XML and <code>Rock&amp;Roll</code> for text files.
     */
    public abstract String toOriginal();
}
