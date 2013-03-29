/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Didier Briel
               2012 Guido Leenders
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

package org.omegat.filters3.xml.resx;

import org.omegat.filters2.Instance;
import org.omegat.filters2.Shortcuts;
import org.omegat.filters3.xml.XMLFilter;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;
import org.xml.sax.Attributes;

/**
 * Filter for ResX files.
 * 
 * @author Didier Briel
 * @author Guido Leenders
 */
public class ResXFilter extends XMLFilter {

    private String id = "";
    private String entryText;
    private String comment;
    private String text;

    /**
     * Creates a new instance of ResXFilter
     */
    public ResXFilter() {
        super(new ResXDialect());
    }

    /**
     * Human-readable name of the File Format this filter supports.
     * 
     * @return File format name
     */
    public String getFileFormatName() {
        return OStrings.getString("RESX_FILTER_NAME");
    }

    /**
     * The default list of filter instances that this filter class has. One filter class may have different
     * filter instances, different by source file mask, encoding of the source file etc.
     * <p>
     * There is one pattern for when no source language or source culture are present in the filename, one
     * for when only a source language is present and one for when both source language and source culture 
     * are present.
     * In all three cases, the source language and/or source culture are eaten from the filename, assuming
     * the source language/culture use '.' (dot) as the separator.
     * <p>
     * Note that the user may change the instances freely.
     * 
     * @return Default filter instances
     */
    public Instance[] getDefaultInstances() {
        return new Instance[]{ 
            new Instance("*.??-??.resx", null, null, "${nameOnly}.${targetLocaleLCID}.resx"),
            new Instance("*.??.resx", null, null, "${nameOnly}.${targetLocaleLCID}.resx"),
            new Instance("*.resx", null, null, "${nameOnly}.${targetLocaleLCID}.resx")
        };
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
     * Yes, ResX may be written out in a variety of encodings.
     * 
     * @return <code>true</code>
     */
    @Override
    public boolean isTargetEncodingVariable() {
        return true;
    }

    @Override
    public void tagStart(String path, Attributes atts) {
        if ("/root/data".equals(path)) {
            id = StringUtil.nvl(atts.getValue("name"), "");
            comment = null;
        }
    }

    @Override
    public void tagEnd(String path) {
        if ("/root/data/comment".equals(path)) {
            comment = text;
        } else if ("/root/data".equals(path)) {
            if (entryParseCallback != null) {
                entryParseCallback.addEntry(id, entryText, null, false, comment, null, this, null);
            }
            id = null;
            entryText = null;
            comment = null;
        }
    }

    @Override
    public void text(String text) {
        this.text = text;
    }

    @Override
    public String translate(String entry, Shortcuts shortcutDetails) {
        if (entryParseCallback != null) {
            entryText = entry;
            return entry;
        } else {
            String trans = entryTranslateCallback.getTranslation(id, entry, null);
            return trans != null ? trans : entry;
        }
    }
}
