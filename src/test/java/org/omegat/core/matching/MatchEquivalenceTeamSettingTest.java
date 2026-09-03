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

package org.omegat.core.matching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectSettingsStorage;
import org.omegat.core.data.TeamSetting;
import org.omegat.core.data.TeamSettingsRegistry;
import org.omegat.util.OStrings;

/**
 * The disabled equivalence classes ride the generic team settings sidecar
 * (SF #1681): absent value means every class active, the normalizer folds
 * the default to null and drops unknown ids, and a default project writes
 * no file.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class MatchEquivalenceTeamSettingTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private ProjectProperties props;
    private TeamSetting setting;

    @Before
    public void setUp() throws Exception {
        props = new ProjectProperties(folder.getRoot());
        setting = MatchEquivalence.teamSetting();
    }

    @After
    public void tearDown() {
        TeamSettingsRegistry.unregister(MatchEquivalence.TEAM_SETTING_KEY);
    }

    @Test
    public void defaultReadsAsNull() {
        assertNull(setting.read(props));
        assertNull(setting.normalize(""));
        assertNull(setting.normalize(null));
    }

    @Test
    public void normalizerCanonicalizesAndDropsUnknownIds() {
        assertEquals("dashes", setting.normalize("dashes,ligatures"));
        assertEquals("quotes,spaces", setting.normalize(" spaces , quotes "));
        assertEquals("quotes", setting.normalize("QUOTES"));
        assertNull(setting.normalize("ligatures"));
    }

    @Test
    public void describerNamesDisabledClasses() {
        assertEquals(OStrings.getString("EQUIVALENCE_TEAM_SETTING_ALL_ACTIVE"), setting.describe(null));
        String described = setting.describe("dashes");
        assertTrue(described, described.contains(MatchEquivalence.DASHES.getLocalizedName()));
    }

    @Test
    public void applyAndReadRoundTrip() {
        setting.apply(props, "quotes,invisibles");
        assertEquals(EnumSet.of(MatchEquivalence.QUOTES, MatchEquivalence.INVISIBLES),
                props.getDisabledMatchEquivalences());
        assertEquals("invisibles,quotes", setting.read(props));
        // The applier is an idempotent setter: a repeated value is a no-op.
        setting.apply(props, "invisibles,quotes");
        assertEquals("invisibles,quotes", setting.read(props));
        // A value of only unknown ids applies as the default.
        setting.apply(props, "ligatures");
        assertTrue(props.getDisabledMatchEquivalences().isEmpty());
        setting.apply(props, null);
        assertTrue(props.getDisabledMatchEquivalences().isEmpty());
        assertNull(setting.read(props));
    }

    @Test
    public void storageRoundTripAndDefaultWritesNoFile() throws Exception {
        // Mirror of the persist guard in RealProject: save runs only for a
        // configured value or an already existing file, so default projects
        // stay sidecar-free.
        persistLikeRealProject();
        assertFalse("default project must stay sidecar-free",
                ProjectSettingsStorage.getFile(props).isFile());

        setting.apply(props, "dashes");
        persistLikeRealProject();
        assertEquals("dashes",
                setting.normalize(ProjectSettingsStorage.load(props, MatchEquivalence.TEAM_SETTING_KEY)));
    }

    private void persistLikeRealProject() throws Exception {
        String sessionValue = setting.read(props);
        if (sessionValue != null || ProjectSettingsStorage.getFile(props).isFile()) {
            ProjectSettingsStorage.save(props, MatchEquivalence.TEAM_SETTING_KEY, sessionValue);
        }
    }

    @Test
    public void registrationIsIdempotent() {
        MatchEquivalence.registerTeamSetting();
        MatchEquivalence.registerTeamSetting();
        assertEquals(1, TeamSettingsRegistry.all().stream()
                .filter(s -> MatchEquivalence.TEAM_SETTING_KEY.equals(s.getKey())).count());
    }
}
