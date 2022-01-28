/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2012 Guido Leenders, Thomas Cordonnier
               2013 Aaron Madlon-Kay
               2014 Alex Buloichik, Aaron Madlon-Kay
               2021-2022 Thomas Cordonnier
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
 * @author Thomas Cordonnier
 */
public class DefaultProjectTMXEntry extends ProjectTMXEntry {
    public final String source;

    DefaultProjectTMXEntry(ITMXEntry from, ExternalLinked linked) {
        super(from, linked);

        this.source = from.getSourceText();
    }

    @Override
    public boolean isDefaultTranslation() {
        return false;
    }
    
    public String getSourceText() {
        return source;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (! super.equals(obj)) {
            return false;
        }
        try {
            ITMXEntry other = (ITMXEntry) obj;
            return other.getSourceText().equals(this.source);
        } catch (ClassCastException cc) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(changeDate / 1000, creationDate / 1000, translation, note, linked, changer, creator,
                source);
    }
    
    public String getPropValue(String propType) {
        if (ProjectTMX.PROP_XAUTO.equals(propType)) {
            switch (linked) {
                case xAUTO: case xENFORCED: return "auto";
            }
        }
        return null; // for the moment internal entries do not store properties
    }
    
    public boolean hasPropValue(String propType, String propValue) {
        if (ProjectTMX.PROP_XAUTO.equals(propType)) {
            switch (linked) {
                case xAUTO: case xENFORCED: return "auto".equals(propValue);
            }
        }
        return false; // for the moment internal entries do not store properties
    }
    
    public List<TMXProp> getProperties() {
        if (linked == null) {
            return null;
        } else {
             return List.of(new TMXProp(ProjectTMX.PROP_XAUTO, "auto"));
        }
    }
}
