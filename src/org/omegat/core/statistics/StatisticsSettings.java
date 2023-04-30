/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Alex Buloichik
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

package org.omegat.core.statistics;

import org.omegat.util.Preferences;

/**
 * Class for get/set statistics preferences and settings.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class StatisticsSettings {

    private StatisticsSettings() {
    }

    public static boolean isCountingProtectedText() {
        return Preferences.isPreferenceDefault(Preferences.STAT_COUNTING_PROTECTED_TEXT,
                Preferences.STAT_COUNTING_PROTECTED_TEXT_DEFAULT);
    }

    public static void setCountingProtectedText(boolean value) {
        Preferences.setPreference(Preferences.STAT_COUNTING_PROTECTED_TEXT, value);
    }

    public static boolean isCountingStandardTags() {
        return false;
    }

    public static boolean isCountingCustomTags() {
        return Preferences.isPreferenceDefault(Preferences.STAT_COUNTING_CUSTOM_TAGS,
                Preferences.STAT_COUNTING_CUSTOM_TAGS_DEFAULT);
    }

    public static void setCountingCustomTags(boolean value) {
        Preferences.setPreference(Preferences.STAT_COUNTING_CUSTOM_TAGS, value);
    }
}
