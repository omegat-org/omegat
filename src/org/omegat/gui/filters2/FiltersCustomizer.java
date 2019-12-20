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

package org.omegat.gui.filters2;

import java.awt.Window;

import org.omegat.gui.dialogs.PreferencesDialog;

import gen.core.filters.Filters;

/**
 * A convenience class implementing a similar API to the old FiltersCutomizer implementation, now based on
 * {@link FiltersCustomizerController} and {@link PreferencesDialog}.
 *
 * @author Aaron Madlon-Kay
 */
public class FiltersCustomizer {

    private final FiltersCustomizerController view;
    private final PreferencesDialog dialog;

    public FiltersCustomizer(boolean projectSpecific, Filters defaultFilters,
            Filters userFilters, Filters projectFilters) {
        this.view = new FiltersCustomizerController(projectSpecific, defaultFilters, userFilters, projectFilters);
        this.dialog = new PreferencesDialog(view);
    }

    public boolean show(Window parent) {
        return dialog.show(parent);
    }

    public Filters getResult() {
        return view.getResult();
    }
}
