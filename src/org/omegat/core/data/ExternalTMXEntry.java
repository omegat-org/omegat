/*
 * *************************************************************************
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2022 Hiroshi Miura
 *                Home page: http://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  *************************************************************************
 *
 */

package org.omegat.core.data;

import java.util.Collections;
import java.util.List;

import org.omegat.util.TMXProp;

/**
 * Entry class to hold external TM under project's tm/ data.
 * It is designated to be immutable POJO class to bring TM data in ProjectTMX.
 *
 * CAUTION:
 * Currently it is inherited from PrepareTMXEntry for backward binary compatibility.
 * When OmegaT 6.x reached, we will drop inheritance from PrepareTMXEntry and
 * all fields are defined as private-final in this class.
 */
public class ExternalTMXEntry extends PrepareTMXEntry implements ITMXEntry {

    /*
    private final String source;
    private final String translation;
    private final String changer;
    private final long changeDate;
    private final String creator;
    private final long creationDate;
    private final String note;
    private final List<TMXProp> otherProperties;
     */

    ExternalTMXEntry(final PrepareTMXEntry from, final List<TMXProp> prop) {
        this.source = from.source;
        this.translation = from.translation;
        this.changer = from.changer;
        this.changeDate = from.changeDate;
        this.creator = from.creator;
        this.creationDate = from.creationDate;
        this.note = from.note;
        this.otherProperties = prop;
    }

    /**
     * Gets the source text
     */
    @Override
    public String getSourceText() {
        return source;
    }

    /**
     * Gets translation text
     */
    @Override
    public String getTranslationText() {
        return translation;
    }

    /**
     * Gets the initial creator of the entry
     */
    @Override
    public String getCreator() {
        return creator;
    }

    /**
     * Gets the initial creation date as an EPOCH timestamp
     */
    @Override
    public long getCreationDate() {
        return creationDate;
    }

    /**
     * Gets the author of last change in the entry
     */
    @Override
    public String getChanger() {
        return changer;
    }

    /**
     * Gets the EPOCH timestamp for last change in this entry
     */
    @Override
    public long getChangeDate() {
        return changeDate;
    }

    /**
     * Gets text note (markup &lt;note&gt; in TMX format)
     */
    @Override
    public String getNote() {
        return note;
    }

    /**
     * Gets TMX properties.
     * @return immutable list of TMX properties.
     */
    public List<TMXProp> getProperties() {
        return Collections.unmodifiableList(otherProperties);
    }
}
