/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2012 Guido Leenders, Thomas Cordonnier
               2013 Aaron Madlon-Kay
               2014 Alex Buloichik, Aaron Madlon-Kay
               2021 Thomas Cordonnier
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
public class ExternalTMXEntry extends TMXEntry {
    public List<TMXProp> otherProperties;

    ExternalTMXEntry(ITMXEntry from) {
        super(from);

        this.otherProperties = from.getProperties();
    }

    @Override
    public boolean equals(Object obj) {
        if (! super.equals(obj)) {
            return false;
        }
        // Now compare properties
        try {
            ITMXEntry other = (ITMXEntry) obj;
            if (other.getProperties() == null) {
                return (this.otherProperties == null) || (this.otherProperties.size() == 0);
            } else if (this.otherProperties == null) {
                return (other.getProperties() == null) || (other.getProperties().size() == 0);
            } else if (other.getProperties().size() != this.otherProperties.size()) {
                return false;
            } else {
                // same properties in same order
                for (int i = 0; i < otherProperties.size(); i++) {
                    if (! otherProperties.get(i).equals(other.getProperties().get(i))) {
                        return false;
                    }
                }
            }
            return true;
        } catch (ClassCastException cc) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(changeDate / 1000, creationDate / 1000, translation, note, changer, creator,
                source);
    }
    
    public boolean hasProperties() {
        return (otherProperties != null) && (otherProperties.size() > 0);
    }
    
    public List<TMXProp> getProperties() {
        return Collections.unmodifiableList(otherProperties);
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
}
