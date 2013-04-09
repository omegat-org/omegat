/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
           (C) 2010 Didier Briel
 
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

package org.omegat.filters3.xml.l10nmgr;

import java.util.regex.Pattern;

import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * This class specifies the L10nmgr XML Dialect.
 * 
 * @author Didier Briel
 */
public class L10nmgrDialect extends DefaultXMLDialect {
    public static final Pattern TYPO3L10N = Pattern.compile("TYPO3L10N");

    public L10nmgrDialect() {

        defineConstraint(CONSTRAINT_ROOT, TYPO3L10N);

        defineParagraphTags(new String[] { 
            "pageGrp", // Specific L10nmgr tag
            "data",    // Specific L10nmgr tag
            // HML tags
            "title", "address", "blockquote", "center", "div", "h1", "h2", "h3", "h4", "h5", "table", "th",
            "tr", "td", "p", "ol", "ul", "li", "dl", "dt", "dd", "form", "textarea", "fieldset", "legend",
            "label", "select", "option", "hr"
        });

        defineIntactTags(new String[] { "head", });

        // L10nmgr requires empty tags to have a closing tag
        // E.g., <title></title> instead of <title/>
        setClosingTagRequired(true);
    }

}
