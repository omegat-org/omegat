/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/omegat/omegat.html
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

package org.omegat.filters3.xml.xhtml;

import java.util.regex.Pattern;

import org.omegat.filters2.Instance;
import org.omegat.filters3.xml.XMLFilter;
import org.omegat.util.OStrings;

/**
 * Filter for XHTML files.
 *
 * @author Maxym Mykhalchuk
 */
public class XHTMLFilter extends XMLFilter
{
    
    /**
     * Creates a new instance of DocBookFilter
     */
    public XHTMLFilter()
    {
        super(new XHTMLDialect());
    }

    /**
     * Human-readable name of the File Format this filter supports.
     * 
     * @return File format name
     */
    public String getFileFormatName()
    {
        return OStrings.getString("XHTML_FILTER_NAME");
    }

    /**
     * The default list of filter instances that this filter class has.
     * One filter class may have different filter instances, different
     * by source file mask, encoding of the source file etc.
     * <p>
     * Note that the user may change the instances freely.
     * 
     * @return Default filter instances
     */
    public Instance[] getDefaultInstances()
    {
        return new Instance[] 
        {
            new Instance("*.html", "UTF-8", "UTF-8"),                           // NOI18N
            new Instance("*.xhtml", "UTF-8", "UTF-8"),                          // NOI18N
        };
    }

    /**
     * Yes, DocBook may be read in a variety of encodings.
     * @return <code>true</code>
     */
    public boolean isSourceEncodingVariable()
    {
        return true;
    }
    
    /**
     * Yes, DocBook may be written out in a variety of encodings.
     * @return <code>true</code>
     */
    public boolean isTargetEncodingVariable()
    {
        return true;
    }
}
