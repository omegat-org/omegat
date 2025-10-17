/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               2025 Hiroshi Miura
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

import java.util.Collections;
import java.util.List;

import org.omegat.core.data.CoreState;
import org.omegat.gui.exttrans.IMachineTranslation;
import org.omegat.util.Log;

/**
 * A class for aggregating machine translation connectors.
 * <p>
 * You can register your MT plugin through a Core method as follows;
 * <code>
 * public class ExamplePlugin {
 *     public static void loadPlugins() {
 *         Core.registerMachineTranslationClass(ExamplePlugin.class);
 *     }
 *     public static void unloadPlugins() {
 *     }
 *     public ExamplePlugin() {
 *         // You can initialize internal resources here.
 *         // Because the class will be instantiated in a dynamic way through
 *         // Core.registerMachineTranslationClass API, only a default constructor
 *         // can be used, and unable to expect static initialization of the class.
 *     }
 * }
 * </code>
 *
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 */
@Deprecated(since = "6.1.0", forRemoval = true)
@SuppressWarnings("unused")
public final class MachineTranslators {

    private MachineTranslators() {
    }

    public static void add(IMachineTranslation machineTranslator) {
        MachineTranslatorsManager mt = CoreState.getInstance().getMachineTranslatorsManager();
        if (mt == null) {
            Log.logDebug("MachineTranslatorsManager is null.");
            return;
        }
        mt.add(machineTranslator);
    }

    public static List<IMachineTranslation> getMachineTranslators() {
        MachineTranslatorsManager mt = CoreState.getInstance().getMachineTranslatorsManager();
        if (mt == null) {
            Log.logDebug("MachineTranslatorsManager is null.");
            return Collections.emptyList();
        }
        return mt.getMachineTranslators();
    }
}
