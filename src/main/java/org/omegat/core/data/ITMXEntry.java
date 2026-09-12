/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Thomas Cordonnier
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

package org.omegat.core.data;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.omegat.util.TMXProp;

/**
 * Common interface for any object storing a pair source / translation text
 * with date and author
 *
 * @author Thomas Cordonnier
 */
public interface ITMXEntry extends ITranslationEntry {

    /**
     * Gets the initial creator of the entry
     */
    @Nullable
    String getCreator();

    /**
     * Gets the initial creation date as an EPOCH timestamp
     */
    long getCreationDate();

    /**
     * Gets the author of last change in the entry
     */
    @Nullable
    String getChanger();

    /**
     * Gets the EPOCH timestamp for last change in this entry
     */
    long getChangeDate();

    /**
     * Gets text note (markup &lt;note&gt; in TMX format)
     */
    @Nullable
    String getNote();

    default boolean hasNote() {
        return getNote() != null;
    }

    /* --------------------- Properties ------------ */

    boolean hasProperties();

    @Nullable
    String getPropValue(String propType);

    boolean hasPropValue(String propType, String propValue);

    @Nullable
    List<TMXProp> getProperties();
}
