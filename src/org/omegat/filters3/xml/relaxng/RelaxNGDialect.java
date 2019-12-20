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

package org.omegat.filters3.xml.relaxng;

import java.util.regex.Pattern;

import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * This class specifies the RELAX NG XML Dialect.
 *
 * RELAX NG is a schema language for XML.  See http://relaxng.org/
 *
 * @author Tony Graham
 */
public class RelaxNGDialect extends DefaultXMLDialect {
    public static final Pattern RELAXNG_ROOT_TAG = Pattern.compile("grammar");
    public static final Pattern RELAXNG_XMLNS = Pattern
            .compile("xmlns(:\\w+)?=\"http://relaxng.org/ns/structure/1.0\"");

    public RelaxNGDialect() {
        defineConstraint(CONSTRAINT_ROOT, RELAXNG_ROOT_TAG);
        defineConstraint(CONSTRAINT_XMLNS, RELAXNG_XMLNS);

        defineParagraphTags(new String[] { "documentation", "a:documentation", });

        defineIntactTags(new String[] { "value", "name", "nsName", });
    }

}
