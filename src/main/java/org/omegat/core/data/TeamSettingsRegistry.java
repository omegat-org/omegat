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

package org.omegat.core.data;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jspecify.annotations.Nullable;

/**
 * Registry of team-negotiated project settings. Feature code registers its
 * {@link TeamSetting} at plugin load; core iterates registered settings when
 * loading, saving and distributing projects. Empty registry means every
 * related code path stays no-op.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public final class TeamSettingsRegistry {

    private static final List<TeamSetting> SETTINGS = new CopyOnWriteArrayList<>();

    private TeamSettingsRegistry() {
    }

    /** Registers setting; rejects duplicate key. */
    public static void register(TeamSetting setting) {
        if (byKey(setting.getKey()) != null) {
            throw new IllegalArgumentException("Team setting already registered: " + setting.getKey());
        }
        SETTINGS.add(setting);
    }

    /** Removes setting with given key, for plugin unload and tests. */
    public static void unregister(String key) {
        SETTINGS.removeIf(s -> s.getKey().equals(key));
    }

    /** All registered settings, in registration order. */
    public static List<TeamSetting> all() {
        return List.copyOf(SETTINGS);
    }

    /** Setting with given key, null when unknown. */
    public static @Nullable TeamSetting byKey(String key) {
        return SETTINGS.stream().filter(s -> s.getKey().equals(key)).findFirst().orElse(null);
    }
}
