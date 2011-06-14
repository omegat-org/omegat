/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik

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

package org.omegat.filters3.xml.android;

import org.omegat.filters2.Instance;
import org.omegat.filters3.xml.XMLFilter;
import org.omegat.util.OStrings;
import org.xml.sax.Attributes;

/**
 * Filter for Android resources.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class AndroidFilter extends XMLFilter {

    private String id, idPlurals = "", comment, idComment;

    public AndroidFilter() {
        super(new AndroidDialect());
    }

    /** Human-readable filter name. */
    public String getFileFormatName() {
        return OStrings.getString("Android_FILTER_NAME");
    }

    /** Extensions... */
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.xml") };
    }

    /** Source encoding can not be varied by the user. */
    public boolean isSourceEncodingVariable() {
        return false;
    }

    /** Target encoding can not be varied by the user. */
    public boolean isTargetEncodingVariable() {
        return false;
    }

    public void tagStart(String path, Attributes atts) {
        if (atts != null) {
            if ("/resources/string".equals(path)) {
                id = atts.getValue("name");
                idComment = comment;
            } else if ("/resources/plurals".equals(path)) {
                idPlurals = atts.getValue("name");
            } else if ("/resources/plurals/item".equals(path)) {
                id = idPlurals + '/' + atts.getValue("quantity");
                idComment = comment;
            }
        }
    }

    public void tagEnd(String path) {
        comment = null;
        if ("/resources/string".equals(path)) {
            idComment = null;
        } else if ("/resources/plurals/item".equals(path)) {
            idComment = null;
        }
    }

    public void comment(String comment) {
        this.comment = comment;
    }

    /**
     * Filter-specific chars processing.
     */
    public String translate(String entry) {
        String e = entry.replace("\\'", "'");
        String r = null;
        if (entryParseCallback != null) {
            entryParseCallback.addEntry(id, e, null, false, idComment, null, this);
            r = e;
        } else if (entryTranslateCallback != null) {
            r = entryTranslateCallback.getTranslation(id, e, null);
            if (r == null) {
                r = e;
            }
        }
        return r.replace("'", "\\'");
    }
}
