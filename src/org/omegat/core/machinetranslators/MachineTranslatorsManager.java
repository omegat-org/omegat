/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.core.machinetranslators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.exttrans.IMTGlossarySupplier;
import org.omegat.gui.exttrans.IMachineTranslation;
import org.omegat.util.Log;

/**
 * A class for aggregating machine translation connectors.
 *
 * @author Aaron Madlon-Kay
 *
 */
public class MachineTranslatorsManager {

    private final List<IMachineTranslation> machineTranslations = new ArrayList<>();

    public MachineTranslatorsManager() {
        for (Class<?> mtc : PluginUtils.getMachineTranslationClasses()) {
            try {
                machineTranslations.add((IMachineTranslation) mtc.getDeclaredConstructor().newInstance());
            } catch (Exception ex) {
                Log.log(ex);
            }
        }
    }

    /**
     * Set the glossary map for all machine translation connectors.
     * @param supplier The glossary map supplier to set.
     */
    public void setGlossaryMap(IMTGlossarySupplier supplier) {
        for (IMachineTranslation mt : getMachineTranslators()) {
            mt.setGlossarySupplier(supplier);
        }
    }

    /**
     * Get all machine translation connectors.
     * @return An immutable list of machine translation connectors.
     */
    public List<IMachineTranslation> getMachineTranslators() {
        return Collections.unmodifiableList(machineTranslations);
    }
}
