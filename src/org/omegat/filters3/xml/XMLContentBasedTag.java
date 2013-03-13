/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.filters3.xml;

import org.omegat.util.StringUtil;

/**
 * XML content based tag for create shortcuts based on content, not on tag name.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class XMLContentBasedTag extends XMLIntactTag {
    private String shortcut;

    /** Creates a new instance of XML Tag */
    public XMLContentBasedTag(String tag, String shortcut, org.xml.sax.Attributes attributes) {
        super(tag, shortcut, attributes);
    }

    /** Returns the short form of this tag, most often -- the first letter. */
    public String getShortcut() {
        char shortcut;
        if (intactContents.size() > 0 && (intactContents.get(0) instanceof XMLText)) {
            XMLText xmlText = (XMLText) intactContents.get(0);
            shortcut = StringUtil.getFirstLetterLowercase(xmlText.getText());
            if (shortcut == 0) {
                shortcut = 'f';
            }
        } else {
            shortcut = getTag().charAt(0);
        }
        return Character.toString(shortcut);
    }

    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }

    public String toShortcut() {
        return shortcut;
    }
}
