/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2010 Didier Briel
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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.filters3.xml.l10nmgr;

import org.omegat.filters2.Instance;
import org.omegat.filters3.xml.XMLFilter;
import org.omegat.util.OStrings;

/**
 * Filter for L10nmgr for Typo3.
 * 
 * @author Didier Briel
 */
public class L10nmgrFilter extends XMLFilter {

    /**
     * Creates a new instance of L10nmgrFilter
     */
    public L10nmgrFilter() {
        super(new L10nmgrDialect());
    }

    /**
     * {@inheritDoc}
     */
    public String getFileFormatName() {
        return OStrings.getString("L10NMGR_FILTER_NAME");
    }

    /**
     * {@inheritDoc}
     */
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.xml", null, null), };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSourceEncodingVariable() {
        return false;
    }

    /**
     * Yes, L10nmgr may be written out in a variety of encodings.
     * 
     * @return <code>true</code>
     */
    @Override
    public boolean isTargetEncodingVariable() {
        return true;
    }

    @Override
    protected boolean requirePrevNextFields() {
        return true;
    }
}
