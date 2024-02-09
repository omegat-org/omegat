/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Didier Briel, Guido Leenders
               2015 Tony Graham
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

package org.omegat.filters3.xml.schematron;

import org.omegat.core.Core;
import org.omegat.filters2.Instance;
import org.omegat.filters3.xml.XMLFilter;
import org.omegat.util.OStrings;

/**
 * Filter for Java Schematron XML files.
 *
 * @author Tony Graham
 */
public class SchematronFilter extends XMLFilter {

    /**
     * Register plugin into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerFilterClass(SchematronFilter.class);
    }

    public static void unloadPlugins() {
    }

    /**
     * Creates a new instance of Schematron
     */
    public SchematronFilter() {
        super(new SchematronDialect());
    }

    /**
     * Human-readable name of the File Format this filter supports.
     *
     * @return File format name
     */
    @Override
    public String getFileFormatName() {
        return OStrings.getString("SCHEMATRON_FILTER_NAME");
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
    @Override
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.sch", null, null), };
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
     * Yes, Java Schematron XML may be written out in a variety of encodings.
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
     * Schematron element and namespace constraints.
     *
     * @return <code>true</code> or <code>false</code>
     */
    /*
    public boolean isFileSupported(BufferedReader reader) {
        XMLDialect dialect = getDialect();
        if (dialect.getConstraints() == null || dialect.getConstraints().isEmpty()) {
            return true;
        }
        try {
            char[] cbuf = new char[OConsts.READ_AHEAD_LIMIT];
            int cbuf_len = reader.read(cbuf);
            String buf = new String(cbuf, 0, cbuf_len);
            Matcher matcher = SchematronDialect.SCHEMATRON_XMLNS.matcher(buf);
        if (!matcher.find()) // not Schematron
        return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    */
}
