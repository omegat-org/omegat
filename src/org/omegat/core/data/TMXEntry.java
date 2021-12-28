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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
 */
public class TMXEntry {
    public enum ExternalLinked {
        // declares how this entry linked to external TMX in the tm/auto/
        xICE, x100PC, xAUTO, xENFORCED
    };

    public final String source;
    public final String translation;
    public final String changer;
    public final long changeDate;
    public final String creator;
    public final long creationDate;
    public final String note;
    public List<TMXProp> otherProperties;
    public final boolean defaultTranslation;
    public final ExternalLinked linked;

    TMXEntry(TMXEntry from, boolean defaultTranslation, ExternalLinked linked) {
        this.source = from.source;
        this.translation = from.translation;
        this.changer = from.changer;
        this.changeDate = from.changeDate;
        this.creator = from.creator;
        this.creationDate = from.creationDate;
        this.note = from.note;
        this.linked = linked;
        this.defaultTranslation = defaultTranslation;
    }

    TMXEntry(PrepareTMXEntry from, boolean defaultTranslation, ExternalLinked linked) {
        this.source = from.source;
        this.translation = from.translation;
        this.changer = from.changer;
        this.changeDate = from.changeDate;
        this.creator = from.creator;
        this.creationDate = from.creationDate;
        this.note = from.note;

        this.defaultTranslation = defaultTranslation;
        this.linked = linked;
    }

    public boolean isTranslated() {
        return translation != null;
    }

    public boolean hasNote() {
        return note != null;
    }

    public String getPropValue(String propType) {
        if (otherProperties == null) {
            return null;
        }
        for (int i = 0; i < otherProperties.size(); i++) {
            TMXProp kv = otherProperties.get(i);
            if (propType.equals(kv.getType())) {
                return kv.getValue();
            }
        }
        return null;
    }

    public boolean hasPropValue(String propType, String propValue) {
        if (otherProperties == null) {
            return false;
        }
        for (int i = 0; i < otherProperties.size(); i++) {
            TMXProp kv = otherProperties.get(i);
            if (propType.equals(kv.getType())) {
                if (propValue == null) {
                    return true;
                }
                if (propValue.equals(kv.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    public Iterable<TMXProp> getProperties() {
        return Collections.unmodifiableCollection(otherProperties);
    }

    public ExternalLinked getLinked() {
        return linked;
    }

    public boolean isDefaultTranslation() {
        return defaultTranslation;
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
        if (!Objects.equals(source, other.source)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(changeDate / 1000, creationDate / 1000, translation, note, linked, changer, creator,
                defaultTranslation, source);
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
        if (!Objects.equals(linked, other.linked)) {
            return false;
        }
        return true;
    }

    /* provide builder for TMXEntry.
       other classes can build TMXEntry by calling such as
       TMXEntry::Builder.setSource(source).setTranslation(translation).build();
     */
    private TMXEntry(final String source, final String translation, final String changer, final long changeDate,
                     final String creator, final long creationDate, final String note,
                     final List<TMXProp> otherProperties,
                     final boolean defaultTranslation,
                     final ExternalLinked linked) {
        this.source = source;
        this.translation = translation;
        this.changer = changer;
        this.changeDate = changeDate;
        this.creator = creator;
        this.creationDate = creationDate;
        this.note = note;
        this.otherProperties = otherProperties;
        this.defaultTranslation = defaultTranslation;
        this.linked = linked;
    }

    /**
     * TMXEntry object builder.
     */
    public static final class Builder {
        private String source;
        private String translation;
        private String changer;
        private long changeDate;
        private String creator;
        private long creationDate;
        private String note;
        public List<TMXProp> otherProperties;
        private boolean defaultTranslation;
        private ExternalLinked linked;

        public Builder() {
        }

        /**
         * build method for TMXEntry object.
         * @return TMXEntry object.
         */
        public TMXEntry build() {
            return new TMXEntry(source, translation, changer, changeDate, creator, creationDate, note,
                    otherProperties, defaultTranslation, linked);
        }

        /**
         * Set source text.
         * @param source text.
         * @return builder.
         */
        public Builder setSource(final String source) {
            this.source = source;
            return this;
        }

        /**
         * Set translation text.
         * @param translation text.
         * @return builder.
         */
        public Builder setTranslation(final String translation) {
            this.translation = translation;
            return this;
        }

        /**
         * Set changer name.
         * @param changer name.
         * @return builder.
         */
        public Builder setChanger(final String changer) {
            this.changer = changer;
            return this;
        }

        /**
         * Set change date.
         * @param changeDate long date value.
         * @return builder.
         */
        public Builder setChangeDate(final long changeDate) {
            this.changeDate = changeDate;
            return this;
        }

        /**
         * Set creator name
         * @param creator name.
         * @return builder.
         */
        public Builder setCreator(final String creator) {
            this.creator = creator;
            return this;
        }

        /**
         * Set creation date.
         * @param creationDate long value of date.
         * @return builder.
         */
        public Builder setCreationDate(final long creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        /**
         * Set note.
         * @param note text.
         * @return builder.
         */
        public Builder setNote(final String note) {
            this.note = note;
            return this;
        }

        /**
         * Set whether it is default translation.
         * @param defaultTranslation true when defualt translation.
         * @return builder.
         */
        public Builder setDefaultTranslation(final boolean defaultTranslation) {
            this.defaultTranslation = defaultTranslation;
            return this;
        }

        /**
         * Set external link.
         * @param linked external link.
         * @return builder.
         */
        public Builder setExternalLinked(final ExternalLinked linked) {
            this.linked = linked;
            return this;
        }

        public Builder setProperties(final List<TMXProp> properties) {
            otherProperties = properties;
            return this;
        }

        public Builder setProperty(String propType, String propValue) {
            if (otherProperties == null) {
                otherProperties = new ArrayList<>();
            }
            otherProperties.add(new TMXProp(propType, propValue));
            return this;
        }
    }
}
