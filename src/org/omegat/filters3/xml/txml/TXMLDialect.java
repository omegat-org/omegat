/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2011-2012 Didier Briel

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

package org.omegat.filters3.xml.txml;

import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * This class specifies the TXML XML Dialect.
 *
 * @author Didier Briel
 */
public class TXMLDialect extends DefaultXMLDialect {
    public TXMLDialect() {
        defineParagraphTags(new String[] { "source", "target", });

        defineIntactTags(new String[] { "source", "ut", "skeleton", "revisions"});

    }

}
