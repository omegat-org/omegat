/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Didier Briel
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

package org.omegat.filters3.xml.typo3;

import java.io.BufferedReader;
import java.util.regex.Matcher;

import org.omegat.filters2.Instance;
import org.omegat.filters3.xml.XMLDialect;
import org.omegat.filters3.xml.XMLFilter;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;

/**
 * Filter for Typo3 LocManager files.
 * 
 * @author Didier Briel
 */
public class Typo3Filter extends XMLFilter {

    /**
     * Creates a new instance of Typo3Filter
     */
    public Typo3Filter() {
        super(new Typo3Dialect());
    }

    /**
     * Human-readable name of the File Format this filter supports.
     * 
     * @return File format name
     */
    public String getFileFormatName() {
        return OStrings.getString("TYPO3_FILTER_NAME");
    }

    /**
     * The default list of filter instances that this filter class has. One
     * filter class may have different filter instances, different by source
     * file mask, encoding of the source file etc.
     * <p>
     * Note that the user may change the instances freely.
     * 
     * @return Default filter instances
     */
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.xml", null, null), };
    }

    /**
     * Either the encoding can be read, or it is UTF-8.
     * 
     * @return <code>false</code>
     */
    public boolean isSourceEncodingVariable() {
        return false;
    }

    /**
     * Yes, Typo3 LocManager may be written out in a variety of encodings.
     * 
     * @return <code>true</code>
     */
    public boolean isTargetEncodingVariable() {
        return true;
    }
    
    @Override
    protected boolean requirePrevNextFields() {
        return true;
    }

    /**
     * Returns whether the file is supported by the filter, by checking root
     * tags constraints.
     * 
     * @return <code>true</code> or <code>false</code>
     */
    public boolean isFileSupported(BufferedReader reader) {
        XMLDialect dialect = getDialect();
        if (dialect.getConstraints() == null || dialect.getConstraints().size() == 0)
            return true;
        try {
            char[] cbuf = new char[OConsts.READ_AHEAD_LIMIT];
            int cbuf_len = reader.read(cbuf);
            String buf = new String(cbuf, 0, cbuf_len);
            Matcher matcher = Typo3Dialect.TYPO3_ROOT_TAG.matcher(buf);
            if (matcher.find()) { // This a Typo3 main page...
                return true;
            } else { // Let's see if we have Typo3 secondary page...
                matcher = Typo3Dialect.TYPO3_ROOT_TAG2.matcher(buf);
                if (!matcher.find()) // Neither kind of page
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
