/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2012 Guido Leenders, Thomas Cordonnier
               2013 Aaron Madlon-Kay
               2014 Alex Buloichik, Aaron Madlon-Kay
               2021 Hiroshi Miura
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.data;

import java.util.Arrays;
import java.util.Objects;

import org.omegat.util.TMXProp;

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
 * @author Hiroshi Miura
 */
public class TMXEntry {
    public enum ExternalLinked {
        // declares how this entry linked to external TMX in the tm/auto/
        xICE, x100PC, xAUTO, xENFORCED
    };

    public enum Prop {
        MTSOURCE(0);
        public final int idx;
        Prop(int i) {
            idx = i;
        }
    }

    public final String source;
    public final String translation;
    public final String changer;
    public final long changeDate;
    public final String creator;
    public final long creationDate;
    public final String note;
    private final String[] props = new String[Prop.values().length];
    public final boolean defaultTranslation;
    public final ExternalLinked linked;

    TMXEntry(PrepareTMXEntry from, boolean defaultTranslation, ExternalLinked linked) {
        this.source = from.source;
        this.translation = from.translation;
        this.changer = from.changer;
        this.changeDate = from.changeDate;
        this.creator = from.creator;
        this.creationDate = from.creationDate;
        this.note = from.note;
        if (from.otherProperties != null) {
            for (int i = 0; i < from.otherProperties.size(); i++) {
                TMXProp prop = from.otherProperties.get(i);
                if (prop.getType().equals("mtsource")) {
                    props[Prop.MTSOURCE.idx] = prop.getValue();
                }
            }
        }
        this.defaultTranslation = defaultTranslation;
        this.linked = linked;
    }

    public boolean has(final Prop key) {
        return props[key.idx] != null;
    }

    public String get(final Prop key) {
        return props[key.idx];
    }

    public void set(final Prop key, final String value) {
        props[key.idx] = value;
    }

    public boolean isTranslated() {
        return translation != null;
    }

    public boolean hasNote() {
        return note != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TMXEntry other = (TMXEntry) obj;
        /*
         * Dates can't be just checked for equals since date stored in memory with 1 milliseconds accuracy,
         * but written to file with 1 second accuracy.
         */
        if (changeDate / 1000 != other.changeDate / 1000) {
            return false;
        }
        if (creationDate / 1000 != other.creationDate / 1000) {
            return false;
        }
        if (!equalsTranslation(other)) {
            return false;
        }
        if (!Objects.equals(changer, other.changer)) {
            return false;
        }
        if (!Objects.equals(creator, other.creator)) {
            return false;
        }
        if (defaultTranslation != other.defaultTranslation) {
            return false;
        }
        return Objects.equals(source, other.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(changeDate / 1000, creationDate / 1000, translation, note, linked, changer, creator,
                Arrays.hashCode(props), defaultTranslation, source);
    }

    /**
     * Two TMXEntrys are considered interchangeable if this method returns true,
     * even if equals() != true.
     */
    public boolean equalsTranslation(TMXEntry other) {
        if (other == null) {
            return false;
        }
        if (!Objects.equals(translation, other.translation)) {
            return false;
        }
        if (!Objects.equals(note, other.note)) {
            return false;
        }
        if (!Objects.equals(props[Prop.MTSOURCE.idx], other.props[Prop.MTSOURCE.idx])) {
            return false;
        }
        return Objects.equals(linked, other.linked);
    }
}
