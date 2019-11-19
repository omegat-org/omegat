/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Didier Briel, Guido Leenders
               2012 Guido Leenders
               2015 Tony Graham

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

package org.omegat.filters3.xml.schematron;

import java.util.HashMap;
import java.util.regex.Pattern;

import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * This class specifies the Schematron XML Dialect.
 *
 * @author Tony Graham
 */
public class SchematronDialect extends DefaultXMLDialect {
    public static final Pattern SCHEMATRON_ROOT_TAG = Pattern.compile("schema|pattern");
    public static final Pattern SCHEMATRON_XMLNS = Pattern
            .compile("xmlns(:\\w+)?=\"http://purl.oclc.org/dsdl/schematron\"");

    /*
     * A map of attribute-name and attribute value pairs that, if exist in a
     * tag, indicate that this tag should not be translated
     */
    private HashMap<String, String> ignoreTagsAttributes;

    public SchematronDialect() {
        defineConstraint(CONSTRAINT_ROOT, SCHEMATRON_ROOT_TAG);
        //defineConstraint(CONSTRAINT_XMLNS, SCHEMATRON_XMLNS);

        defineParagraphTags(new String[] { "assert", "report", });

        defineIntactTags(new String[] { "phase", "active", "ns", "include", "key", "let", });

        ignoreTagsAttributes = new HashMap<String, String>();
        ignoreTagsAttributes.put("TRANSLATE=FALSE", "");
    }

}
