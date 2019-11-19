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

package org.omegat.gui.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * A class for aggregating preferences controllers. Plugins should add their
 * suppliers (these can simply be constructor method references, i.e.
 * <code>MyClass::new</code>) via {@link #addSupplier(Supplier)}.
 *
 * @author Aaron Madlon-Kay
 */
public final class PreferencesControllers {
    static final List<Supplier<IPreferencesController>> CONTROLLER_SUPPLIERS = new ArrayList<>();

    private PreferencesControllers() {
    }

    public static void addSupplier(Supplier<IPreferencesController> view) {
        CONTROLLER_SUPPLIERS.add(view);
    }

    public static List<Supplier<IPreferencesController>> getSuppliers() {
        return Collections.unmodifiableList(CONTROLLER_SUPPLIERS);
    }
}
