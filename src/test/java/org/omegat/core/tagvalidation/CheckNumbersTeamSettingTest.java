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

package org.omegat.core.tagvalidation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectSettingsStorage;
import org.omegat.core.data.TeamSetting;
import org.omegat.core.data.TeamSettingsRegistry;

/**
 * The numeral check rides the generic team settings sidecar as opt-out flag
 * (SF #465): absent value means enabled, only a disabled check materialises
 * in the file, and the registration survives repeated bootstraps.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class CheckNumbersTeamSettingTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private ProjectProperties props;
    private TeamSetting setting;

    @Before
    public void setUp() throws Exception {
        props = new ProjectProperties(folder.newFolder("project"));
        TagValidation.registerCheckNumbersTeamSetting();
        setting = TeamSettingsRegistry.byKey(TagValidation.CHECK_NUMBERS_TEAM_SETTING_KEY);
    }

    @After
    public void tearDown() {
        TeamSettingsRegistry.unregister(TagValidation.CHECK_NUMBERS_TEAM_SETTING_KEY);
    }

    @Test
    public void enabledIsDefaultAndReadsAsNull() {
        assertTrue(props.isCheckNumbersEnabled());
        assertNull(setting.read(props));
        assertNull(setting.normalize("true"));
        assertEquals("false", setting.normalize(" FALSE "));
    }

    @Test
    public void applyAndReadRoundTrip() {
        setting.apply(props, "false");
        assertFalse(props.isCheckNumbersEnabled());
        assertEquals("false", setting.read(props));
        setting.apply(props, null);
        assertTrue(props.isCheckNumbersEnabled());
        assertNull(setting.read(props));
    }

    @Test
    public void storageRoundTripAndDefaultWritesNoFile() throws Exception {
        // Mirror of the persist guard in RealProject: save runs only for a
        // configured value or an already existing file.
        persistLikeRealProject();
        assertFalse("default project must stay sidecar-free",
                ProjectSettingsStorage.getFile(props).isFile());

        setting.apply(props, "false");
        persistLikeRealProject();
        assertEquals("false", setting.normalize(
                ProjectSettingsStorage.load(props, TagValidation.CHECK_NUMBERS_TEAM_SETTING_KEY)));
    }

    @Test
    public void registrationIsIdempotent() {
        TagValidation.registerCheckNumbersTeamSetting();
        TagValidation.registerCheckNumbersTeamSetting();
        assertEquals(1, TeamSettingsRegistry.all().stream()
                .filter(s -> TagValidation.CHECK_NUMBERS_TEAM_SETTING_KEY.equals(s.getKey())).count());
    }

    private void persistLikeRealProject() throws Exception {
        String sessionValue = setting.read(props);
        if (sessionValue != null || ProjectSettingsStorage.getFile(props).isFile()) {
            ProjectSettingsStorage.save(props, TagValidation.CHECK_NUMBERS_TEAM_SETTING_KEY, sessionValue);
        }
    }
}
