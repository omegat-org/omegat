/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2010 Didier Briel
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

package org.omegat.filters3.xml.wix;

import java.util.List;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.xml.sax.Attributes;

import org.omegat.core.Core;
import org.omegat.core.data.ProtectedPart;
import org.omegat.filters2.Instance;
import org.omegat.filters3.xml.XMLFilter;
import org.omegat.util.OStrings;

/**
 * Filter for WiX resources.
 *
 * @author Didier Briel
 */
@NullMarked
public class WiXFilter extends XMLFilter {
    private @Nullable String id;

    /**
     * Register plugin into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerFilterClass(WiXFilter.class);
    }

    public static void unloadPlugins() {
    }

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
    public void tagStart(@Nullable String path, @Nullable Attributes atts) {
        if (atts == null) {
            return;
        }
        id = atts.getValue("Id");
    }

    @Override
    public String translate(String entry, @Nullable List<ProtectedPart> protectedParts) {
        if (entryParseCallback != null) {
            entryParseCallback.addEntry(id, entry, null, false, null, null, this, protectedParts);
            return entry;
        } else if (entryTranslateCallback != null) {
            String trans = entryTranslateCallback.getTranslation(id, entry, null);
            return trans != null ? trans : entry;
        } else {
            return entry;
        }
    }
}
