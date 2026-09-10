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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Contract of the boolean team setting descriptor and the registry: absent
 * key means default, only the non-default value materialises, normalization
 * folds explicit default values back to null so divergence detection never
 * confuses "explicitly default" with "not configured".
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class TeamSettingTest {

    private static final String KEY = "test_option";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @After
    public final void tearDown() {
        TeamSettingsRegistry.unregister(KEY);
    }

    @Test
    public void testOptInBooleanContract() throws Exception {
        AtomicBoolean holder = new AtomicBoolean(false);
        TeamSetting setting = TeamSetting.ofBoolean(KEY, "TEAM_SETTING_DIVERGED_TITLE", false,
                config -> holder.get(), (config, value) -> holder.set(value));
        ProjectProperties config = new ProjectProperties(folder.newFolder("p"));

        assertNull("default reads as not configured", setting.read(config));
        holder.set(true);
        assertEquals("only the non-default value materialises", "true", setting.read(config));

        setting.apply(config, null);
        assertEquals("null applies the default", false, holder.get());
        setting.apply(config, "true");
        assertTrue(holder.get());

        assertNull("explicit default folds back to null", setting.normalize("false"));
        assertEquals("true", setting.normalize("true"));
        assertNull(setting.normalize(null));
    }

    @Test
    public void testOptOutBooleanContract() throws Exception {
        AtomicBoolean holder = new AtomicBoolean(true);
        TeamSetting setting = TeamSetting.ofBoolean(KEY, "TEAM_SETTING_DIVERGED_TITLE", true,
                config -> holder.get(), (config, value) -> holder.set(value));
        ProjectProperties config = new ProjectProperties(folder.newFolder("p"));

        assertNull("default reads as not configured", setting.read(config));
        holder.set(false);
        assertEquals("false", setting.read(config));

        setting.apply(config, null);
        assertTrue("null applies the default", holder.get());
        assertNull("explicit default folds back to null", setting.normalize("true"));
        assertEquals("false", setting.normalize("false"));
    }

    @Test
    public void testDisplayNameStripsMnemonicMarker() {
        // existing label keys carry mnemonic markers; dialogs must not show them
        TeamSetting setting = TeamSetting.ofBoolean(KEY, "PP_REMOVE_TAGS", false,
                config -> false, (config, value) -> {
                });
        assertFalse("marker key must resolve to a non-empty name", setting.getDisplayName().isEmpty());
        assertEquals("mnemonic marker must be stripped", -1, setting.getDisplayName().indexOf('&'));
    }

    @Test
    public void testRegistryRejectsDuplicatesAndUnregisters() {
        TeamSetting setting = TeamSetting.ofBoolean(KEY, "TEAM_SETTING_DIVERGED_TITLE", false,
                config -> false, (config, value) -> {
                });
        TeamSettingsRegistry.register(setting);
        assertNotNull(TeamSettingsRegistry.byKey(KEY));
        assertThrows(IllegalArgumentException.class, () -> TeamSettingsRegistry.register(setting));
        TeamSettingsRegistry.unregister(KEY);
        assertNull(TeamSettingsRegistry.byKey(KEY));
        assertTrue(TeamSettingsRegistry.all().stream().noneMatch(s -> s.getKey().equals(KEY)));
    }
}
