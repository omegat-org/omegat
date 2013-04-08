/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2013 Didier Briel
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.filters3.xml;

import org.omegat.filters3.Attribute;
import org.omegat.filters3.Tag;
import org.omegat.gui.editor.EditorUtils;
import org.omegat.util.Language;

/**
 * XML Tag.
 * 
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 */
public class XMLTag extends Tag {
    /** Creates a new instance of XML Tag */
    public XMLTag(String tag, String shortcut, Type type, org.xml.sax.Attributes attributes, Language targetLanguage) {
        super(tag, shortcut, type, XMLUtils.convertAttributes(attributes));     
        this.targetLanguage = targetLanguage;
    }

    private Language targetLanguage;
    /**
     * Returns the tag in its original form as it was in original document. E.g.
     * for &lt;strong&gt; tag should return &lt;strong&gt;.
     * Do specific processing for Open XML documents
     */
    public String toOriginal() {
        StringBuffer buf = new StringBuffer();

        buf.append("<");
        if (Type.END == getType())
            buf.append("/");
        buf.append(getTag());
        buf.append(getAttributes().toString());
        
        // If that's an Open XML document, we preserve spaces for all <w:t> tags 
        if (getTag().equalsIgnoreCase("w:t") && Type.BEGIN == getType()) {
            Boolean preserve = false;
            for (int i = 0; i < getAttributes().size(); i++) {
                Attribute oneAttribute = getAttributes().get(i);
                if (oneAttribute.getName().equalsIgnoreCase("xml:space")) { // If XML:space is already there
                    preserve = true; // We do nothing
                    break;
                }
            }
            if (!preserve) {
                 buf.append(" xml:space=\"preserve\"");
            }
        }    
        
        boolean alreadyClosed = false;
        
        // If the target language is RTL and the document is a .doxc 
        // we do a number of tag insertions
        if (EditorUtils.isRTL(targetLanguage.getLanguageCode())) {
            if (getTag().equalsIgnoreCase("w:pPr") && Type.BEGIN == getType()) {
                buf.append("><w:bidi/");
            } else if (getTag().equalsIgnoreCase("w:sectPr") && Type.BEGIN == getType()) {
                buf.append("><w:bidi/");
            } else if (getTag().equalsIgnoreCase("w:rPr") && Type.BEGIN == getType()) {
                buf.append("><w:rtl/");
            } else if (getTag().equalsIgnoreCase("w:tblPr") && Type.BEGIN == getType()) {
                buf.append("><w:bidiVisual/");
            } else if (getTag().equalsIgnoreCase("w:tblStyle") && Type.ALONE == getType()) {
                buf.append("/><w:bidiVisual/"); 
                alreadyClosed = true;
            }
        }

        if (Type.ALONE == getType() && !alreadyClosed) {
            buf.append("/");
        }
        buf.append(">");

        return buf.toString();
    }

}
