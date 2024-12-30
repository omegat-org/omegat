/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2013 Didier Briel
               2020 Briac Pilpre
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

import org.omegat.filters3.Attribute;
import org.omegat.filters3.Tag;
import org.omegat.util.BiDiUtils;
import org.omegat.util.Language;

/**
 * XML Tag.
 *
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Briac Pilpre
 */
public class XMLTag extends Tag {
    private Language sourceLanguage;
    private Language targetLanguage;

    /** Creates a new instance of XML Tag */
    public XMLTag(String tag, String shortcut, Type type, org.xml.sax.Attributes attributes, Translator translator) {
        super(tag, shortcut, type, XMLUtils.convertAttributes(attributes));
        this.sourceLanguage = translator.getSourceLanguage();
        this.targetLanguage = translator.getTargetLanguage();
    }

    /**
     * Returns the tag in its original form as it was in original document. E.g.
     * for &lt;strong&gt; tag should return &lt;strong&gt;.
     * Do specific processing for Open XML documents
     */
    public String toOriginal() {
        StringBuilder buf = new StringBuilder();

        boolean isRtl = BiDiUtils.isRtl(targetLanguage.getLanguageCode());
        boolean differentDir = isDifferentDirection(isRtl);
        boolean isSpecialDocxTagLTR = isSpecialDocxBidiTag(false);
        boolean isSpecialDocxTagRTL = isSpecialDocxBidiTag(true);

        // Skip special (i/b/sz) tags for target language to keep only those from the source language
        // (e.g don't include <w:bCs/> from the LTR source in the RTL target.)
        if (differentDir && ((isRtl && isSpecialDocxTagRTL) || (!isRtl && isSpecialDocxTagLTR))) {
            return "";
        }

        buf.append("<");
        if (Type.END == getType()) {
            buf.append("/");
        }

        // In Docx, the bold, italic and fontSize are handled differently for complex
        // script
        if (differentDir && isRtl && isSpecialDocxTagLTR) {
            // For LTR -> RTL, convert the i/b/sz to iCs/bCs/szCs if source and target
            // languages directionality are different.
            buf.append(getTag() + "Cs");
        } else if (differentDir && !isRtl && isSpecialDocxTagRTL) {
            // For RTL -> LTR, convert the iCs/bCs/szCs to i/b/sz
            buf.append(getTag().replaceFirst("Cs", ""));
        } else {
            buf.append(getTag());
        }

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
        if (isRtl) {
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

    private boolean isDifferentDirection(boolean isRtl) {
        if (sourceLanguage == null) {
            return false;
        }

        return isRtl != BiDiUtils.isRtl(sourceLanguage.getLanguageCode());
    }

    private boolean isSpecialDocxBidiTag(boolean complex) {
        String suffix = complex ? "Cs" : "";
        return Type.ALONE == getType() && (getTag().equalsIgnoreCase("w:i" + suffix)
                || getTag().equalsIgnoreCase("w:b" + suffix) || getTag().equalsIgnoreCase("w:sz" + suffix));
    }

}
