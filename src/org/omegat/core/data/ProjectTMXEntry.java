/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2012 Guido Leenders, Thomas Cordonnier
               2013 Aaron Madlon-Kay
               2014 Alex Buloichik, Aaron Madlon-Kay
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

import java.util.List;
import java.util.Objects;

import org.omegat.util.TMXProp;

/**
 * Storage for TMX entry for project memory
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
public class ProjectTMXEntry extends TMXEntry {
    public enum ExternalLinked {
        // declares how this entry linked to external TMX in the tm/auto/
        xICE, x100PC, xAUTO, xENFORCED
    };

    public final boolean defaultTranslation;
    public final ExternalLinked linked;

    ProjectTMXEntry(ITMXEntry from, boolean defaultTranslation, ExternalLinked linked) {
        super(from);

        this.defaultTranslation = defaultTranslation;
        this.linked = linked;
    }

    @Override
    public boolean equals(Object obj) {
        if (! super.equals(obj)) {
            return false;
        }
        try {
            ProjectTMXEntry other = (ProjectTMXEntry) obj;
            if (defaultTranslation != other.defaultTranslation) {
                return false;
            }
            return true;
        } catch (ClassCastException cc) {
            return false;
        }
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
        if (! super.equalsTranslation(other)) {
            return false;
        }
        try {
            ProjectTMXEntry pOther = (ProjectTMXEntry) other;
            if (!Objects.equals(linked, pOther.linked)) {
                return false;
            }
        } catch (ClassCastException cc) {
            return false;
        }
        return true;
    }
    
    public boolean hasProperties() {
        return false;
    }
    
    public String getPropValue(String propType) {
        return null; // for the moment internal entries do not store properties
    }
    
    public boolean hasPropValue(String propType, String propValue) {
        return false; // for the moment internal entries do not store properties
    }
    
    public List<TMXProp> getProperties() {
        return null; // for the moment internal entries do not store properties
    }
}
