/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.gui.editor.filter;

import java.awt.Component;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.IEditorFilter;

/**
 * Editor filter that limits the editor to the segments counted in one match
 * statistics category (repetitions, exact match, a fuzzy match band, or no
 * match).
 *
 * @author stephan.pakebusch at zollsoft.de
 */
@NullMarked
public class MatchRangeFilter implements IEditorFilter {
    private final Set<Integer> entriesList;
    private final FilterBarMatchRange controlComponent;

    public MatchRangeFilter(String categoryLabel, Collection<Integer> entries) {
        entriesList = new HashSet<>(entries);
        controlComponent = new FilterBarMatchRange(categoryLabel, entriesList.size());
        controlComponent.btnRemoveFilter.addActionListener(e -> {
            // Make sure that any change done in the current segment is not
            // lost
            Core.getEditor().commitAndDeactivate();
            Core.getEditor().removeFilter();
        });
    }

    @Override
    public boolean isSourceAsEmptyTranslation() {
        return false;
    }

    @Override
    public boolean allowed(@Nullable SourceTextEntry ste) {
        if (ste == null) {
            return false;
        }
        return entriesList.contains(ste.entryNum());
    }

    @Override
    public Component getControlComponent() {
        return controlComponent;
    }
}
