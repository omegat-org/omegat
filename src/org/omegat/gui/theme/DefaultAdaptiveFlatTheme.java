/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 miurahr.
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
 */

package org.omegat.gui.theme;

import java.io.IOException;

import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;

import org.omegat.util.Log;
import org.omegat.util.OStrings;

public class DefaultAdaptiveFlatTheme extends DelegatingLookAndFeel {

    private static final String NAME = OStrings.getString("THEME_OMEGAT_ADAPTIVE_NAME");

    public DefaultAdaptiveFlatTheme() throws Exception {
        super();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getID() {
        return "OmegaT"; // NOI18N
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public UIDefaults getDefaults() {
        String id = systemLookAndFeel.getID();
        UIDefaults defaults = systemLookAndFeel.getDefaults();
        //
        if (id.equals("Aqua") && AppearanceManager.isDarkPreference(defaults)) {
            if (AppearanceManager.isDarkTheme(defaults)) {
                // JDK14 and later support dark mode on Aqua
                return DefaultFlatTheme.setDefaults(defaults, id);
            }
            // Preference is dark but theme is light.
            // It seems OmegaT run on < JRE14
            // set custom dark theme.
            return setDarkDefaults(defaults);
        } else if (id.equals("GTK")) {
            // Linux GTK look and feel follows system color.
            return DefaultFlatTheme.setDefaults(defaults, id);
        } else if (id.equals("Windows") && AppearanceManager.isDarkPreference(defaults)) {
            return setDarkDefaults(defaults);
        }
        return DefaultFlatTheme.setDefaults(defaults, id);
    }

    /**
     * Apply theme default values to the supplied {@link UIDefaults} object.
     * Suitable for third-party themes that want to modify the default theme.
     *
     * @param defaults
     *            the {@link UIDefaults} object such as from
     *            {@link LookAndFeel#getDefaults()}
     * @return the modified {@link UIDefaults} object
     */
    public static UIDefaults setDarkDefaults(UIDefaults defaults) {
        // load default colors
        try {
            AppearanceManager.loadDefaultSystemDarkColors(defaults);
        } catch (IOException e) {
            Log.log(e);
        }
        try {
            AppearanceManager.loadDefaultAppDarkColors(defaults);
        } catch (IOException e) {
            Log.log(e);
        }
        return defaults;
    }

}