/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Thomas Cordonnier
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

/**
 * Common interface for any object storing a pair source / translation text
 * with date and author
 *
 * @author Thomas Cordonnier
 */
public interface ITMXEntry {

    /**
     * Gets the source text
     */
    String getSourceText();

    /**
     * Gets translation text
     */
    String getTranslationText();

    /**
     * Check whenever there is a translation
     */
    default boolean isTranslated() {
        return getTranslationText() != null;
    }

    /**
     * Gets the initial creator of the entry
     */
    String getCreator();

    /**
     * Gets the initial creation date as an EPOCH timestamp
     */
    long getCreationDate();

    /**
     * Gets the author of last change in the entry
     */
    String getChanger();

    /**
     * Gets the EPOCH timestamp for last change in this entry
     */
    long getChangeDate();

    /**
     * Gets text note (markup &lt;note&gt; in TMX format)
     */
    String getNote();

}