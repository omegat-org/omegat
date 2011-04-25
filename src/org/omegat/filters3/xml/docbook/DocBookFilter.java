/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Fabian Mandelbaum, Didier Briel
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

package org.omegat.filters3.xml.docbook;

import java.io.BufferedReader;
import java.util.regex.Matcher;

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
     * Creates a new instance of DocBookFilter
     */
    public DocBookFilter() {
        super(new DocBookDialect());
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
        if (dialect.getConstraints() == null || dialect.getConstraints().size() == 0)
            return true;
        try {
            char[] cbuf = new char[OConsts.READ_AHEAD_LIMIT];
            int cbuf_len = reader.read(cbuf);
            String buf = new String(cbuf, 0, cbuf_len);
            Matcher matcher = DocBookDialect.DOCBOOK_PUBLIC_DTD.matcher(buf);
            if (matcher.find()) { // We can safely assume we have a db4 doc...
                return true;
            } else { // Let's see if we have a db5 doc...
                matcher = DocBookDialect.DB5_XMLNS.matcher(buf);
                if (!matcher.find()) // Neither db4, nor db5
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
