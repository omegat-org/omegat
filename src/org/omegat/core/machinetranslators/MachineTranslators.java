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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.omegat.gui.exttrans.IMachineTranslation;

/**
 * A class for aggregating machine translation connectors. Old-style plugins
 * ("OmegaT-Plugin: machinetranslator") are added here by the
 * MachineTranslateTextArea and so should not add themselves manually. New-style
 * plugins ("OmegaT-Plugins: &lt;classname&gt;") should add themselves with
 * {@link #add(IMachineTranslation)} in the loadPlugins() method.
 *
 * @author Aaron Madlon-Kay
 *
 */
public final class MachineTranslators {

    private static final List<IMachineTranslation> TRANSLATORS = new CopyOnWriteArrayList<>();

    private MachineTranslators() {
    }

    public static void add(IMachineTranslation machineTranslator) {
        TRANSLATORS.add(machineTranslator);
    }

    public static List<IMachineTranslation> getMachineTranslators() {
        return Collections.unmodifiableList(TRANSLATORS);
    }
}
