/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2012 Guido Leenders, Thomas Cordonnier
               2013 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.core.data;

import org.omegat.util.StringUtil;

import java.util.Collections;
import java.util.Map;

/**
 * Storage for TMX entry.
 * 
 * Variables in this class can be changed only before store to ProjectTMX. After that, all values must be
 * unchangeable.
 * 
 * Only RealProject can create and change TMXEntry objects.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Guido Leenders
 * @author Aaron Madlon-Kay
 */
public class TMXEntry {
    public final String source;
    public final String translation;
    public final String changer;
    public final long changeDate;
    public final String creator;
    public final long creationDate;
    public final String note;
    public final boolean defaultTranslation;
    public final Map<String, String> otherProperties;

    TMXEntry(PrepareTMXEntry from) {
        this.source = from.source;
        this.translation = from.translation;
        this.changer = from.changer;
        this.changeDate = from.changeDate;
        this.creator = from.creator;
        this.creationDate = from.creationDate;
        this.note = from.note;
        this.defaultTranslation = from.defaultTranslation;
        this.otherProperties = from.otherProperties == null ? null : Collections
                .unmodifiableMap(from.otherProperties);
    }

    public boolean isTranslated() {
        return translation != null;
    }

    public boolean hasNote() {
        return note != null;
    }

    public boolean equalsTranslation(TMXEntry other) {
        if (other == null) {
            return false;
        }
        if (changeDate != other.changeDate) {
            return false;
        }
        if (!StringUtil.equalsWithNulls(translation, other.translation)) {
            return false;
        }
        if (!StringUtil.equalsWithNulls(changer, other.changer)) {
            return false;
        }
        if (!StringUtil.equalsWithNulls(note, other.note)) {
            return false;
        }
        return true;
    }
}
