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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.filters3.xml.wix;

import org.omegat.filters2.Instance;
import org.omegat.filters3.xml.XMLFilter;
import org.omegat.util.OStrings;
import org.xml.sax.Attributes;

/**
 * Filter for WiX resources.
 * 
 * @author Didier Briel
 */
public class WiXFilter extends XMLFilter {
    private String id;

    public WiXFilter() {
        super(new WiXDialect());
    }

    /** Human-readable filter name. */
    public String getFileFormatName() {
        return OStrings.getString("WIX_FILTER_NAME");
    }

    /** Extensions... */
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.wxl") };
    }

    /**
     * Either the encoding can be read, or it is UTF-8.
     * 
     * @return <code>false</code>
     */
    @Override
    public boolean isSourceEncodingVariable() {
        return false;
    }

    /**
     * Yes, Wix may be written out in a variety of encodings.
     * 
     * @return <code>true</code>
     */
    @Override
    public boolean isTargetEncodingVariable() {
        return true;
    }

    @Override
    public void tagStart(String path, Attributes atts) {
        id = atts.getValue("Id");
    }

    @Override
    public String translate(String entry) {
        if (entryParseCallback != null) {
            entryParseCallback.addEntry(id, entry, null, false, null, null, this);
            return entry;
        } else {
            String trans = entryTranslateCallback.getTranslation(id, entry, null);
            return trans != null ? trans : entry;
        }
    }
}
