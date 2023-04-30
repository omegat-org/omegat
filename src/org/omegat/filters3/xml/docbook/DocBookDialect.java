/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Didier Briel
               2008 Fabian Mandelbaum, Didier Briel
               2016 Didier Briel
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

package org.omegat.filters3.xml.docbook;

import java.util.regex.Pattern;

import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * This class specifies DocBook XML Dialect.
 *
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Fabian Mandelbaum
 */
public class DocBookDialect extends DefaultXMLDialect {
    public static final Pattern DOCBOOK_PUBLIC_DTD = Pattern.compile("-//OASIS//DTD DocBook.*");
    public static final Pattern DB5_XMLNS = Pattern
            .compile("xmlns(:\\w+)?=\"http://docbook.org/ns/docbook\"");

    public DocBookDialect() {
        defineConstraint(CONSTRAINT_PUBLIC_DOCTYPE, DOCBOOK_PUBLIC_DTD);

        // Some paragraph and preformat tags added because content was missing
        // See https://sourceforge.net/p/omegat/bugs/844/
        defineParagraphTags(new String[] { "book", "bookinfo", "title", "subtitle", "authorgroup", "author",
                       "firstname", "surname", "affiliation", "orgname", "address", "email", "edition", "pubdate",
                       "copyright", "year", "holder", "isbn", "keywordset", "keyword", "preface", "title", "simpara", "para",
                       "chapter", "table", "tgroup", "thead", "tbody", "row", "entry", "revhistory", "revision",
                       "revnumber", "date", "authorinitials", "revremark", "itemizedlist", "listitem", "member",
                       "releaseinfo", "bibliomixed", "bibliomset", "bridgehead", "glossseealso",
                       "primaryie", "refentrytitle", "secondaryie", "seealsoie", "seeie", "subtitle",
                       "synopfragmentref", "term", "tertiaryie", "tocentry", "glosssee", "section"});

        defineOutOfTurnTags(new String[] { "indexterm", });

        definePreformatTags(new String[] { "screen", "programlisting", "synopsis", "literallayout", "address" });

        defineTranslatableAttributes(new String[] { "url", "lang", "xml:lang" });

    }

    // TODO: Can we read db xml content here to try to determinate if
    // the root element has a NS declaration to be able to handle
    // namespaced-tags properly? We'd actually need to read only the
    // root element together with its attributes.
}
