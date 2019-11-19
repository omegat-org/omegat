/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.externalfinder.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The top-level container for ExternalFinder configuration. This class is intended to be immutable.
 *
 * @author Aaron Madlon-Kay
 */
public class ExternalFinderConfiguration {

    // the default value means the items may be placed at the top of popup menu.
    private static final int DEFAULT_POPUP_PRIORITY = 50;

    private final int priority;

    private final List<ExternalFinderItem> items;

    public static ExternalFinderConfiguration empty() {
        return new ExternalFinderConfiguration(DEFAULT_POPUP_PRIORITY, Collections.emptyList());
    }

    public ExternalFinderConfiguration(int priority, List<ExternalFinderItem> items) {
        this.priority = priority >= 0 ? priority : DEFAULT_POPUP_PRIORITY;
        this.items = new ArrayList<>(items);
    }

    public int getPriority() {
        return priority;
    }

    public List<ExternalFinderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((items == null) ? 0 : items.hashCode());
        result = prime * result + priority;
        return result;
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
        ExternalFinderConfiguration other = (ExternalFinderConfiguration) obj;
        if (items == null) {
            if (other.items != null) {
                return false;
            }
        } else if (!items.equals(other.items)) {
            return false;
        }
        if (priority != other.priority) {
            return false;
        }
        return true;
    }
}
