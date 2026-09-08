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

package org.omegat.util.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.omegat.util.Preferences;
import org.omegat.util.PreferencesXML;
import org.omegat.util.TestPreferencesInitializer;
import org.omegat.util.gui.Styles.EditorColor;

/**
 * @author stephan.pakebusch at zollsoft.de
 */
public class ColorRegistryTest {

    @Before
    public void setUp() throws Exception {
        TestPreferencesInitializer.init();
        ColorRegistry.clearPluginEntries();
    }

    @After
    public void tearDown() {
        ColorRegistry.clearPluginEntries();
    }

    @Test
    public void testCoreEntriesOnly() {
        List<ColorEntry> all = ColorRegistry.all();
        assertEquals(EditorColor.values().length, all.size());
        assertSame(EditorColor.COLOR_BACKGROUND, all.get(0));
        assertSame(EditorColor.COLOR_BACKGROUND,
                ColorRegistry.byId(EditorColor.COLOR_BACKGROUND.getId()).orElseThrow(AssertionError::new));
    }

    @Test
    public void testRegisterPluginColor() {
        ColorEntry entry = ColorRegistry.registerPluginColor("myplugin", "highlight", "My Highlight",
                "MyPlugin.highlight", Color.ORANGE);
        assertEquals("myplugin:highlight", entry.getId());
        assertEquals("My Highlight", entry.getDisplayName());
        // No theme defines the UIManager key in tests: the fallback wins.
        assertEquals(Color.ORANGE, entry.getDefault());
        assertEquals(Color.ORANGE, entry.getColor());

        List<ColorEntry> all = ColorRegistry.all();
        assertEquals(EditorColor.values().length + 1, all.size());
        assertSame(entry, all.get(all.size() - 1));
        assertSame(entry, ColorRegistry.byId("myplugin:highlight").orElseThrow(AssertionError::new));
        assertFalse(ColorRegistry.byId("otherplugin:highlight").isPresent());
    }

    @Test
    public void testDuplicateAndEmptyRegistrationRejected() {
        ColorRegistry.registerPluginColor("myplugin", "highlight", "My Highlight", "MyPlugin.highlight",
                Color.ORANGE);
        assertThrows(IllegalArgumentException.class, () -> ColorRegistry.registerPluginColor("myplugin",
                "highlight", "Duplicate", "MyPlugin.highlight", Color.RED));
        assertThrows(IllegalArgumentException.class,
                () -> ColorRegistry.registerPluginColor("", "key", "Name", "Key", Color.RED));
        assertThrows(IllegalArgumentException.class,
                () -> ColorRegistry.registerPluginColor("plugin", "", "Name", "Key", Color.RED));
    }

    @Test
    public void testMalformedIdentifiersRejected() {
        // ":" and "." in the color key would make the id or the preference
        // key ambiguous; a leading digit would break the XML element name.
        assertThrows(IllegalArgumentException.class,
                () -> ColorRegistry.registerPluginColor("myplugin", "bad:key", "Name", "Key", Color.RED));
        assertThrows(IllegalArgumentException.class,
                () -> ColorRegistry.registerPluginColor("myplugin", "bad.key", "Name", "Key", Color.RED));
        assertThrows(IllegalArgumentException.class,
                () -> ColorRegistry.registerPluginColor("my:plugin", "key", "Name", "Key", Color.RED));
        assertThrows(IllegalArgumentException.class,
                () -> ColorRegistry.registerPluginColor("1plugin", "key", "Name", "Key", Color.RED));
        assertThrows(NullPointerException.class,
                () -> ColorRegistry.registerPluginColor("myplugin", "key", "Name", "Key", null));
    }

    @Test
    public void testPluginColorPersistence() {
        ColorEntry entry = ColorRegistry.registerPluginColor("myplugin", "highlight", "My Highlight",
                "MyPlugin.highlight", Color.ORANGE);
        entry.setColor(Color.MAGENTA);
        assertEquals(Color.MAGENTA, entry.getColor());
        // Persisted under the dot-separated key, not the ":"-separated id.
        assertEquals("#ff00ff", Preferences.getPreference("myplugin.highlight"));

        // Resetting stores the default sentinel and follows the default again.
        entry.setColor(null);
        assertEquals(Color.ORANGE, entry.getColor());
        assertEquals("__DEFAULT__", Preferences.getPreference("myplugin.highlight"));

        // Setting a color equal to the default also resets.
        entry.setColor(Color.MAGENTA);
        entry.setColor(new Color(Color.ORANGE.getRGB()));
        assertEquals("__DEFAULT__", Preferences.getPreference("myplugin.highlight"));
    }

    @Test
    public void testOverrideStoredBeforeRegistrationIsPickedUp() {
        // Real lifecycle: the override from the last session exists in the
        // preferences before the plugin registers (plugins load before
        // Preferences.init in production); it must be resolved lazily on
        // first access, not at registration time.
        Preferences.setPreference("myplugin.highlight", "#123456");
        ColorEntry entry = ColorRegistry.registerPluginColor("myplugin", "highlight", "My Highlight",
                "MyPlugin.highlight", Color.ORANGE);
        assertEquals(Color.decode("#123456"), entry.getColor());
    }

    @Test
    public void testPluginPreferenceKeyRoundTripsThroughXml() throws Exception {
        // The id contains ":", which is a namespace separator in XML element
        // names and therefore forbidden as a preference key: omegat.prefs
        // would fail to load and get backed up aside, losing all settings.
        // The dot-separated persistence key must survive a real XML
        // round-trip (the in-memory test preferences would not catch this).
        File file = File.createTempFile("omegat-colorregistry", ".prefs");
        file.deleteOnExit();
        new PreferencesXML(null, file).save(Collections.singletonList("myplugin.highlight"),
                Collections.singletonList("#ff00ff"));
        List<String> keys = new ArrayList<>();
        List<String> values = new ArrayList<>();
        new PreferencesXML(file, null).load(keys, values);
        assertEquals(Collections.singletonList("myplugin.highlight"), keys);
        assertEquals(Collections.singletonList("#ff00ff"), values);
    }

    @Test
    public void testCoreIdsAreEnumNames() {
        for (EditorColor core : EditorColor.values()) {
            assertEquals(core.name(), core.getId());
            assertTrue(ColorRegistry.byId(core.name()).isPresent());
        }
    }
}
