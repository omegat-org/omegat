/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2012 Guido Leenders, Thomas Cordonnier
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

package org.omegat.core.data;

import org.omegat.util.StringUtil;

import java.util.Map;
 
/**
 * Storage for TMX entry.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Guido Leenders
 */
public class TMXEntry {
    public final String source;
    public final String translation;
    public final String changer;
    public final long changeDate;
    public final String note;
    public final boolean defaultTranslation;
    public final Map<String,String> properties;

    public TMXEntry(String source, String translation, String changer, long changeDate, String note,
            boolean defaultTranslation, Map<String,String> properties) {
        this.source = source;
        this.translation = translation;
        this.changer = changer;
        this.changeDate = changeDate;
        this.note = note;
        this.defaultTranslation = defaultTranslation;
        this.properties = properties;
    }

    public TMXEntry(String source, String translation, String changer, long changeDate, String note,
            boolean defaultTranslation) {
        this (source, translation, changer, changeDate, note, defaultTranslation, null);
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
        boolean equals = true;
        if (equals && changeDate != other.changeDate) {
            equals = false;
        }
        if (equals && !StringUtil.equalsWithNulls(translation, other.translation)) {
            equals = false;
        }
        if (equals && !StringUtil.equalsWithNulls(changer, other.changer)) {
            equals = false;
        }
        if (equals && !StringUtil.equalsWithNulls(note, other.note)) {
            equals = false;
        }
        return equals;
    }
}
