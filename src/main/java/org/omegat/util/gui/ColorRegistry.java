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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;
import org.omegat.util.Log;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;

/**
 * Registry of all configurable application colors: the core
 * {@link Styles.EditorColor} entries plus colors contributed by plugins.
 * Registered plugin colors appear automatically in the colors preferences
 * table and take part in color scheme export/import.
 * <p>
 * Plugins register their colors once at plugin load time. Entries cannot be
 * unregistered; like the core entries they live for the session.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public final class ColorRegistry {

    /**
     * Both parts must form a valid XML element name when joined with a dot
     * (see the persistence key below), so they must start with a letter. The
     * color key additionally may not contain dots so that the persistence key
     * maps back to the id unambiguously.
     */
    private static final Pattern PLUGIN_ID_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9_.-]*");
    private static final Pattern COLOR_KEY_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9_-]*");

    private static final Map<String, ColorEntry> PLUGIN_ENTRIES = new LinkedHashMap<>();

    /** Guards PLUGIN_ENTRIES; a private lock instead of the class monitor. */
    private static final Object LOCK = new Object();

    private ColorRegistry() {
    }

    /**
     * Register a plugin-contributed color. The entry gets the id
     * {@code pluginId + ":" + colorKey} (used in color scheme files) and is
     * persisted in the user preferences under {@code pluginId + "." + colorKey}
     * (preference keys are XML element names, which forbid {@code ":"}). A
     * user override stored in the preferences is picked up lazily on the
     * first {@link ColorEntry#getColor()} call after the preferences are
     * initialized, so registering at plugin load time is safe.
     *
     * @param pluginId
     *            stable identifier of the contributing plugin; must start
     *            with a letter, followed by letters, digits, {@code _ . -}
     * @param colorKey
     *            identifier of the color, unique within the plugin; must
     *            start with a letter, followed by letters, digits, {@code _ -}
     * @param displayName
     *            human-readable name shown in the preferences table
     * @param uiManagerKey
     *            UIManager key under which a look and feel may provide a
     *            themed default; may be null when no theme integration exists
     * @param fallbackColor
     *            default when the look and feel defines no themed value
     * @return the registered entry
     * @throws IllegalArgumentException
     *             if an identifier is malformed or the id is already taken
     */
    public static ColorEntry registerPluginColor(String pluginId, String colorKey,
            String displayName, @Nullable String uiManagerKey, Color fallbackColor) {
        if (!PLUGIN_ID_PATTERN.matcher(pluginId).matches()) {
            throw new IllegalArgumentException("Malformed pluginId: " + pluginId);
        }
        if (!COLOR_KEY_PATTERN.matcher(colorKey).matches()) {
            throw new IllegalArgumentException("Malformed colorKey: " + colorKey);
        }
        if (StringUtil.isEmpty(displayName)) {
            throw new IllegalArgumentException("displayName must not be empty");
        }
        Objects.requireNonNull(fallbackColor, "fallbackColor must not be null");
        String id = pluginId + ":" + colorKey;
        synchronized (LOCK) {
            if (PLUGIN_ENTRIES.containsKey(id)) {
                throw new IllegalArgumentException("Plugin color already registered: " + id);
            }
            ColorEntry entry = new PluginColorEntry(id, pluginId + "." + colorKey, displayName,
                    uiManagerKey, fallbackColor);
            PLUGIN_ENTRIES.put(id, entry);
            return entry;
        }
    }

    /**
     * All configurable colors: the core {@link Styles.EditorColor} entries in
     * declaration order, followed by the plugin entries in registration
     * order.
     */
    public static List<ColorEntry> all() {
        List<ColorEntry> all = new ArrayList<>(Arrays.asList(Styles.EditorColor.values()));
        synchronized (LOCK) {
            all.addAll(PLUGIN_ENTRIES.values());
        }
        return Collections.unmodifiableList(all);
    }

    /**
     * Look up an entry by its {@link ColorEntry#getId() id}; empty for
     * unknown ids (e.g. from a color scheme exported by a setup with other
     * plugins).
     */
    public static Optional<ColorEntry> byId(String id) {
        for (Styles.EditorColor core : Styles.EditorColor.values()) {
            if (core.getId().equals(id)) {
                return Optional.of(core);
            }
        }
        synchronized (LOCK) {
            return Optional.ofNullable(PLUGIN_ENTRIES.get(id));
        }
    }

    /** Discard all plugin entries. Test hook only, not for production use. */
    public static void clearPluginEntries() {
        synchronized (LOCK) {
            PLUGIN_ENTRIES.clear();
        }
    }

    /**
     * A plugin-contributed color, resolved with the same semantics as
     * {@link Styles.EditorColor}: the user override is stored in the
     * preferences (under the dot-separated preference key, not the id), the
     * default comes from the UIManager key when the theme defines it and from
     * the built-in fallback otherwise. Because plugins register before the
     * preferences are initialized, the stored override is read lazily on the
     * first {@link #getColor()} call after initialization.
     */
    private static final class PluginColorEntry implements ColorEntry {

        private static final String DEFAULT_COLOR = "__DEFAULT__";

        private final String id;
        private final String prefKey;
        private final String displayName;
        private final @Nullable String uiManagerKey;
        private final Color fallbackColor;
        private @Nullable Color color;
        private boolean prefLoaded;

        PluginColorEntry(String id, String prefKey, String displayName, @Nullable String uiManagerKey,
                Color fallbackColor) {
            this.id = id;
            this.prefKey = prefKey;
            this.displayName = displayName;
            this.uiManagerKey = uiManagerKey;
            this.fallbackColor = fallbackColor;
        }

        private void loadPersistedOverrideOnce() {
            if (prefLoaded || !Preferences.isInitialized()) {
                return;
            }
            prefLoaded = true;
            String prefColor = Preferences.getPreferenceDefault(prefKey, null);
            if (prefColor != null && !DEFAULT_COLOR.equals(prefColor)) {
                try {
                    color = Color.decode(prefColor);
                } catch (NumberFormatException e) {
                    Log.logWarningRB("PREFS_COLOR_VALUE_PARSE_ERROR", displayName, prefColor);
                }
            }
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public Color getColor() {
            loadPersistedOverrideOnce();
            Color current = color;
            return current != null ? current : getDefault();
        }

        @Override
        public Color getDefault() {
            Color themed = uiManagerKey == null ? null : javax.swing.UIManager.getColor(uiManagerKey);
            return themed != null ? themed : fallbackColor;
        }

        @Override
        public void setColor(@Nullable Color newColor) {
            prefLoaded = true;
            if (newColor == null || newColor.equals(getDefault())) {
                color = null;
                Preferences.setPreference(prefKey, DEFAULT_COLOR);
            } else {
                color = newColor;
                Preferences.setPreference(prefKey, String.format("#%02x%02x%02x", newColor.getRed(),
                        newColor.getGreen(), newColor.getBlue()));
            }
        }
    }
}
