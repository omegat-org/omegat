/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;

import org.junit.Before;
import org.junit.Test;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.TestPreferencesInitializer;

public class StylesTest {

    /**
     * Scheme keys consumed straight through {@code UIManager.getColor}
     * instead of a {@link Styles.EditorColor} entry.
     */
    private static final Set<String> DIRECT_UIMANAGER_KEYS = Set.of(
            "OmegaT.projectFilesCurrentFileBackground",
            "OmegaT.projectFilesCurrentFileForeground",
            "OmegaT.searchDimmedBackground",
            "OmegaT.searchFieldErrorText",
            "OmegaT.searchResultBorder");

    @Before
    public void setUp() throws Exception {
        TestPreferencesInitializer.init();
    }

    /**
     * All colors should have a localizable name in Bundle.properties.
     *
     * @throws Exception
     */
    @Test
    public void testColorStrings() throws Exception {
        for (Styles.EditorColor c : Styles.EditorColor.values()) {
            try {
                OStrings.getString(c.name());
            } catch (MissingResourceException t) {
                fail("Color " + c.name() + " does not have an entry in Bundle.properties");
            }
        }
    }

    /**
     * Every color must name the UIManager key under which a theme provides
     * its default, and no two colors may share a key.
     */
    @Test
    public void testUIManagerKeysUniqueAndNonEmpty() {
        Set<String> seen = new HashSet<>();
        for (Styles.EditorColor c : Styles.EditorColor.values()) {
            String key = c.getUIManagerKey();
            assertFalse("Color " + c.name() + " has an empty UIManager key",
                    key == null || key.isEmpty());
            assertTrue("Color " + c.name() + " reuses UIManager key " + key, seen.add(key));
        }
    }

    /**
     * The default must follow the UIManager lazily: a value the installed
     * theme defines wins, and removing it falls back to the built-in
     * fallback. This is the regression test for the old behavior of
     * capturing {@code UIManager.getColor} once at enum-load time.
     */
    @Test
    public void testDefaultFollowsUIManager() {
        Styles.EditorColor c = Styles.EditorColor.COLOR_ACTIVE_SOURCE;
        String key = c.getUIManagerKey();
        Color previous = UIManager.getColor(key);
        try {
            Color themed = new Color(0x123456);
            UIManager.put(key, themed);
            assertEquals(themed, c.getDefault());
            assertEquals(themed, c.getColor());

            UIManager.put(key, null);
            assertEquals(Color.decode("#c0ffc0"), c.getDefault());
        } finally {
            UIManager.put(key, previous);
        }
    }

    /**
     * A user-configured color overrides the default; resetting with
     * {@code setColor(null)} returns to the default.
     */
    @Test
    public void testUserColorOverridesDefault() {
        Styles.EditorColor c = Styles.EditorColor.COLOR_SOURCE;
        try {
            Color custom = new Color(0x654321);
            c.setColor(custom);
            assertEquals(custom, c.getColor());

            c.setColor(null);
            assertEquals(c.getDefault(), c.getColor());
        } finally {
            c.setColor(null);
        }
    }

    /**
     * The built-in fallbacks must stay consistent with the bundled color
     * scheme: every OmegaT color listed in ColorScheme_light.properties
     * falls back to exactly that value, and colors absent from the scheme
     * have no default (consumers treat {@code null} as "inherit"). Also
     * catches a renamed or misspelled key on either side, which would
     * otherwise silently disconnect the theme from the color — the failure
     * mode behind the dead {@code markComesFromTmXendorced}-style keys.
     * Other tests may leave OmegaT keys in the UIManager, so each key is
     * cleared and restored around the fallback check. Finally, every scheme
     * key must have a consumer: either an EditorColor or one of the known
     * direct UIManager lookups, so an orphaned key present in both scheme
     * files cannot go unnoticed.
     */
    @Test
    public void testFallbacksMatchBundledColorScheme() throws Exception {
        Properties light = ResourcesUtil.getBundleColorProperties("light");
        Properties dark = ResourcesUtil.getBundleColorProperties("dark");
        assertEquals("Light and dark color schemes must define the same keys", light.keySet(),
                dark.keySet());

        Set<String> editorColorKeys = new HashSet<>();
        for (Styles.EditorColor c : Styles.EditorColor.values()) {
            editorColorKeys.add(c.getUIManagerKey());
        }
        for (Object schemeKey : light.keySet()) {
            String key = (String) schemeKey;
            assertTrue("Scheme key " + key + " has no consumer; hook it up to an EditorColor, add it to "
                    + "DIRECT_UIMANAGER_KEYS if it is read straight from the UIManager, or drop it from "
                    + "the bundled schemes", editorColorKeys.contains(key) || DIRECT_UIMANAGER_KEYS.contains(key));
        }

        for (Styles.EditorColor c : Styles.EditorColor.values()) {
            String key = c.getUIManagerKey();
            if (!key.startsWith("OmegaT.")) {
                // TextPane.* keys are owned by the look and feel itself.
                continue;
            }
            Object previous = UIManager.get(key);
            UIManager.put(key, null);
            try {
                String schemeValue = light.getProperty(key);
                if (schemeValue != null) {
                    assertEquals("Fallback of " + c.name() + " diverges from ColorScheme_light.properties",
                            Color.decode(schemeValue), c.getDefault());
                } else {
                    assertNull("Color " + c.name() + " has a fallback but no ColorScheme entry; "
                            + "add the key to the bundled schemes or drop the fallback", c.getDefault());
                }
            } finally {
                UIManager.put(key, previous);
            }
        }
    }

    /**
     * The fallback check above parses only the light values; the dark
     * scheme feeds no fallback and would otherwise first be parsed at
     * theme load time. Run it through the production loader, which
     * throws on any malformed value.
     */
    @Test
    public void testDarkSchemeValuesAreParseableColors() throws Exception {
        UIDefaults defaults = new UIDefaults();
        // the loader derives a highlight color from this key
        defaults.put("TextArea.background", Color.DARK_GRAY);
        UIDesignManager.loadDefaultAppDarkColors(defaults);
        for (Object key : ResourcesUtil.getBundleColorProperties("dark").keySet()) {
            assertNotNull("Dark scheme key " + key + " did not load as a color",
                    defaults.getColor((String) key));
        }
    }

    /**
     * Match diff markers carry intrinsic style defaults: struck-through
     * deleted, underlined inserted, active variants additionally bold.
     */
    @Test
    public void testMatchMarkerIntrinsicStyleDefaults() {
        assertEquals(EnumSet.of(Styles.TextStyle.BOLD, Styles.TextStyle.STRIKETHROUGH),
                Styles.EditorColor.COLOR_MATCHES_DEL_ACTIVE.getDefaultTextStyle());
        assertEquals(EnumSet.of(Styles.TextStyle.STRIKETHROUGH),
                Styles.EditorColor.COLOR_MATCHES_DEL_INACTIVE.getDefaultTextStyle());
        assertEquals(EnumSet.of(Styles.TextStyle.BOLD, Styles.TextStyle.UNDERLINE),
                Styles.EditorColor.COLOR_MATCHES_INS_ACTIVE.getDefaultTextStyle());
        assertEquals(EnumSet.of(Styles.TextStyle.UNDERLINE),
                Styles.EditorColor.COLOR_MATCHES_INS_INACTIVE.getDefaultTextStyle());
        assertTrue(Styles.EditorColor.COLOR_TRANSLATED.getDefaultTextStyle().isEmpty());
    }

    /** Only entries with wired call sites expose editable style flags. */
    @Test
    public void testTextStyleablePinnedToWiredCallSites() {
        assertTrue(Styles.EditorColor.COLOR_MATCHES_DEL_ACTIVE.isTextStyleable());
        assertTrue(Styles.EditorColor.COLOR_TRANSLATED.isTextStyleable());
        // underline painter marks, no attribute-based call site yet
        assertFalse(Styles.EditorColor.COLOR_TRANSTIPS.isTextStyleable());
        assertFalse(Styles.EditorColor.COLOR_GLOSSARY_SOURCE.isTextStyleable());
    }

    /**
     * Configured flags round-trip through preferences, reset back to
     * intrinsic defaults.
     */
    @Test
    public void testTextStyleRoundTripAndReset() {
        Styles.EditorColor entry = Styles.EditorColor.COLOR_MATCHES_DEL_INACTIVE;
        Set<Styles.TextStyle> original = EnumSet.noneOf(Styles.TextStyle.class);
        original.addAll(entry.getTextStyle());
        try {
            entry.setTextStyle(EnumSet.of(Styles.TextStyle.ITALIC));
            assertTrue(entry.is(Styles.TextStyle.ITALIC));
            assertFalse(entry.is(Styles.TextStyle.STRIKETHROUGH));
            // overrides store plain booleans, default-following flags the
            // sentinel
            assertEquals("true", Preferences.getPreferenceDefault(
                    "COLOR_MATCHES_DEL_INACTIVE_TEXT_STYLE_ITALIC", null));
            assertEquals("false", Preferences.getPreferenceDefault(
                    "COLOR_MATCHES_DEL_INACTIVE_TEXT_STYLE_STRIKETHROUGH", null));
            assertEquals("__DEFAULT__", Preferences.getPreferenceDefault(
                    "COLOR_MATCHES_DEL_INACTIVE_TEXT_STYLE_BOLD", null));

            entry.setTextStyle(entry.getDefaultTextStyle());
            assertTrue(entry.is(Styles.TextStyle.STRIKETHROUGH));
            assertFalse(entry.is(Styles.TextStyle.ITALIC));
            assertEquals("__DEFAULT__", Preferences.getPreferenceDefault(
                    "COLOR_MATCHES_DEL_INACTIVE_TEXT_STYLE_STRIKETHROUGH", null));
        } finally {
            entry.setTextStyle(original);
        }
    }

    /**
     * Overlay adds configured flags onto base attributes, leaves style-less
     * entries untouched.
     */
    @Test
    public void testOverlayTextStyleAppliesConfiguredFlags() {
        AttributeSet base = Styles.createAttributeSet(Color.BLACK, null, null, null);
        AttributeSet styled = Styles.overlayTextStyle(Styles.EditorColor.COLOR_MATCHES_INS_INACTIVE,
                base);
        assertTrue(StyleConstants.isUnderline(styled));
        assertFalse(StyleConstants.isBold(styled));
        assertEquals(Color.BLACK, StyleConstants.getForeground(styled));
        assertSame("Entry without flags must return the base set unchanged", base,
                Styles.overlayTextStyle(Styles.EditorColor.COLOR_TRANSLATED, base));
        assertSame(base, Styles.overlayTextStyle(null, base));
        // paint-time binding keys survive the overlay copy
        AttributeSet bound = Styles.createBoundAttributeSet(Styles.EditorColor.COLOR_TRANSLATED, null,
                false, false);
        AttributeSet overlaid = Styles.overlayTextStyle(Styles.EditorColor.COLOR_MATCHES_INS_INACTIVE,
                bound);
        assertSame(Styles.EditorColor.COLOR_TRANSLATED,
                overlaid.getAttribute(Styles.EDITOR_COLOR_FOREGROUND));
    }

    /** Attribute composition consumed by match pane. */
    @Test
    public void testCreateStyledAttributeSetCarriesColorAndFlags() {
        AttributeSet attrs = Styles
                .createStyledAttributeSet(Styles.EditorColor.COLOR_MATCHES_DEL_ACTIVE);
        assertEquals(Styles.EditorColor.COLOR_MATCHES_DEL_ACTIVE.getColor(),
                StyleConstants.getForeground(attrs));
        assertTrue(StyleConstants.isBold(attrs));
        assertTrue(StyleConstants.isStrikeThrough(attrs));
        assertFalse(StyleConstants.isUnderline(attrs));
    }
}
