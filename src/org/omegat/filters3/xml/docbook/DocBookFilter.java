/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Fabian Mandelbaum, Didier Briel
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

import java.io.BufferedReader;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import org.omegat.core.Core;
import org.omegat.filters2.Instance;
import org.omegat.filters3.xml.XMLDialect;
import org.omegat.filters3.xml.XMLFilter;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;

/**
 * Filter for DocBook files.
 *
 * @author Maxym Mykhalchuk
 * @author Fabian Mandelbaum
 * @author Didier Briel
 */
public class DocBookFilter extends XMLFilter {

    /**
     * Register plugin into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerFilterClass(DocBookFilter.class);
    }

    public static void unloadPlugins() {
    }

    /**
     * Creates a new instance of DocBookFilter
     */
    public DocBookFilter() {
        super(new DocBookDialect());
        try {
            // DocBook filter requires to validate docbook external DTD.
            // The filter requires internet access.
            // XXX: this is vulnerable for XXE attack.
            setSAXFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
            // XXX: we should take care of external entities
            setSAXFeature("http://xml.org/sax/features/external-general-entities", true);
            // XXX: we should take care of external entities
            setSAXFeature("http://xml.org/sax/features/external-parameter-entities", true);
        } catch (SAXNotSupportedException | SAXNotRecognizedException
                | ParserConfigurationException ignored) {
        }
    }

    /**
     * Human-readable name of the File Format this filter supports.
     *
     * @return File format name
     */
    public String getFileFormatName() {
        return OStrings.getString("DocBook_FILTER_NAME");
    }

    /**
     * Returns the hint displayed while the user edits the filter, and when she
     * adds/edits the instance of this filter. The hint may be any string,
     * preferably in a non-geek language.
     *
     *
     * @return The hint for editing the filter in a non-geek language.
     */
    public String getHint() {
        return OStrings.getString("DocBook_HINT");
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
        return new Instance[] { new Instance("*.xml", null, null), new Instance("*.dbk", null, null), };
    }

    /**
     * Either the encoding can be read, or it is UTF-8..
     *
     * @return <code>false</code>
     */
    public boolean isSourceEncodingVariable() {
        return false;
    }

    /**
     * Yes, DocBook may be written out in a variety of encodings.
     *
     * @return <code>true</code>
     */
    public boolean isTargetEncodingVariable() {
        return true;
    }

    protected boolean requirePrevNextFields() {
        return true;
    }

    /**
     * Returns whether the file is supported by the filter, by checking DB4
     * (DTD) or DB5 (Namespace) constraints.
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
            return DocBookDialect.DOCBOOK_PUBLIC_DTD.matcher(buf).find()
                    || DocBookDialect.DB5_XMLNS.matcher(buf).find();
        } catch (Exception e) {
            return false;
        }
    }
}
