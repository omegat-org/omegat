/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik

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

package org.omegat.filters3.xml.android;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.xml.sax.Attributes;

import org.omegat.core.Core;
import org.omegat.core.data.ProtectedPart;
import org.omegat.filters2.Instance;
import org.omegat.filters3.xml.XMLFilter;
import org.omegat.util.OStrings;

/**
 * Filter for Android resources.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class AndroidFilter extends XMLFilter {
    static final String DO_NOT_TRANSLATE = "do not translate";
    static final String DONT_TRANSLATE = "don't translate";

    static final Set<String> NAMED_TAGS = new HashSet<>(Arrays.asList("/resources/string",
            "/resources/color", "/resources/array", "/resources/string-array", "/resources/integer-array"));

    private String id;
    private String idPlurals = "";
    private String comment;
    private String idComment;

    /**
     * Register plugin into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerFilterClass(AndroidFilter.class);
    }

    public static void unloadPlugins() {
    }

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
            if (NAMED_TAGS.contains(path)) {
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
        if (this.comment == null) {
            this.comment = comment;
        } else {
            this.comment += "\n" + comment;
        }
    }

    /**
     * Filter-specific chars processing.
     */
    public String translate(String entry, List<ProtectedPart> protectedParts) {
        /**
         * Android sources has some entries without translatable="false" but with this comment. Yes, it's
         * dirty hack, but there is no other way.
         */
        if (idComment != null) {
            String c = idComment.toLowerCase(Locale.ENGLISH);
            if (c.contains(DO_NOT_TRANSLATE) || c.contains(DONT_TRANSLATE)) {
                return entry;
            }
        }

        String e = entry.replace("\\'", "'");
        String r = e;
        if (entryParseCallback != null) {
            entryParseCallback.addEntry(id, e, null, false, idComment, null, this, protectedParts);
        } else if (entryTranslateCallback != null) {
            String translation = entryTranslateCallback.getTranslation(id, e, null);
            if (translation != null) {
                r = translation;
            }
        }
        return r.replace("'", "\\'");
    }
}
