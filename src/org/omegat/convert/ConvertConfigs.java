/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.convert;

import java.io.File;

import org.omegat.convert.v20to21.Convert20to21;
import org.omegat.gui.main.MainWindowUI;
import org.omegat.util.Log;
import org.omegat.util.StaticUtils;

/**
 * Check old config versions and convert to current version.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ConvertConfigs {
    public static void convert() {
        File newFilters = new File(StaticUtils.getConfigDir() + "filters.xml");
        if (!newFilters.exists()) {
            File oldFilters = new File(StaticUtils.getConfigDir() + "filters.conf");
            try {
                Convert20to21.convertFiltersConfig(oldFilters, newFilters);
            } catch (Exception ex) {
                Log.log(ex);
            }
        }

        File newUI = new File(StaticUtils.getConfigDir() + MainWindowUI.UI_LAYOUT_FILE);
        if (!newUI.exists()) {
            try {
                ConvertTo213.convertUIConfig(newUI);
            } catch (Exception ex) {
                Log.log(ex);
            }
        }
    }
}
