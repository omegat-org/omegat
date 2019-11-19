/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Didier Briel, Guido Leenders
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

import java.io.BufferedReader;

import org.omegat.filters2.Instance;
import org.omegat.filters3.xml.XMLDialect;
import org.omegat.filters3.xml.XMLFilter;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;

/**
 * Filter for RELAX NG XML files.
 *
 * RELAX NG is a schema language for XML.  See http://relaxng.org/
 *
 * @author Tony Graham
 */
public class RelaxNGFilter extends XMLFilter {

    /**
     * Creates a new instance of RelaxNG
     */
    public RelaxNGFilter() {
        super(new RelaxNGDialect());
    }

    /**
     * Human-readable name of the File Format this filter supports.
     *
     * @return File format name
     */
    public String getFileFormatName() {
        return OStrings.getString("RELAXNG_FILTER_NAME");
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
        return new Instance[] { new Instance("*.rng", null, null), };
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
     * Yes, RELAX NG XML may be written out in a variety of encodings.
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

   /**
    * Returns whether the file is supported by the filter by checking
    * RELAX NG element and namespace constraints.
    *
    * @return <code>true</code> or <code>false</code>
    */
    public boolean isFileSupported(BufferedReader reader) {
        XMLDialect dialect = getDialect();
        if (dialect.getConstraints() == null || dialect.getConstraints().isEmpty()) {
            return true;
        }
        try {
            char[] cbuf = new char[OConsts.READ_AHEAD_LIMIT];
            int cbufLen = reader.read(cbuf);
            String buf = new String(cbuf, 0, cbufLen);
            return RelaxNGDialect.RELAXNG_ROOT_TAG.matcher(buf).find()
                    && RelaxNGDialect.RELAXNG_XMLNS.matcher(buf).find();
        } catch (Exception e) {
            return false;
        }
    }
}
