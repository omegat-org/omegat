/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
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

package org.omegat.filters3.xml;

/**
 * XML content based tag for create shortcuts based on content, not on tag name.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class XMLContentBasedTag extends XMLIntactTag {
    private String shortcut;
    private int shortcutLetter;
    private int shortcutIndex;

    /** Creates a new instance of XML Tag */
    public XMLContentBasedTag(XMLDialect xmlDialect, Handler handler, String tag, String shortcut, Type type,
            org.xml.sax.Attributes attributes) {
        super(xmlDialect, handler, tag, shortcut, type, attributes);
    }

    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }

    public String toShortcut() {
        return shortcut;
    }

    public int getShortcutLetter() {
        return shortcutLetter;
    }

    public void setShortcutLetter(int shortcutLetter) {
        this.shortcutLetter = shortcutLetter;
    }

    public int getShortcutIndex() {
        return shortcutIndex;
    }

    public void setShortcutIndex(int shortcutIndex) {
        this.shortcutIndex = shortcutIndex;
    }
}
