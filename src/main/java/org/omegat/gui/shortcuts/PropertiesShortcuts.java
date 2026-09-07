/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Alex Buloichik, Yu Tang
               2017 Aaron Madlon-Kay
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
package org.omegat.gui.shortcuts;

import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.InputMap;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.jspecify.annotations.Nullable;

import org.omegat.util.Platform;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;

/**
 * The <code>PropertiesShortcuts</code> class represents a persistent set of shortcut.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Yu Tang
 * @author Aaron Madlon-Kay
 */
public class PropertiesShortcuts {

    private static final Logger LOGGER = Logger.getLogger(PropertiesShortcuts.class.getName());

    private static final String BUNDLED_ROOT = "/org/omegat/gui/main/";
    private static final String MAIN_MENU_SHORTCUTS_FILE = "MainMenuShortcuts.properties";
    private static final String EDITOR_SHORTCUTS_FILE = "EditorShortcuts.properties";

    private static class LoadedShortcuts {
        static final PropertiesShortcuts MAIN_MENU_SHORTCUTS = loadBundled(BUNDLED_ROOT, MAIN_MENU_SHORTCUTS_FILE);
        static final PropertiesShortcuts EDITOR_SHORTCUTS = loadBundled(BUNDLED_ROOT, EDITOR_SHORTCUTS_FILE);
    }

    public static PropertiesShortcuts getMainMenuShortcuts() {
        return LoadedShortcuts.MAIN_MENU_SHORTCUTS;
    }

    public static PropertiesShortcuts getEditorShortcuts() {
        return LoadedShortcuts.EDITOR_SHORTCUTS;
    }

    /** Bundled default values, from the classpath. */
    private final Map<String, String> defaults = new HashMap<>();
    /**
     * Per-key user overrides, from the file in the config dir and from
     * {@link #setShortcut}. An entry equal to its default is never held here.
     */
    private final Map<String, String> userOverrides = new HashMap<>();
    private final List<Runnable> changeListeners = new CopyOnWriteArrayList<>();
    /** Base name of the user file in the config dir; null for ad-hoc sets. */
    private @Nullable String userFileName;
    /** Classpath location of the bundled defaults; null for ad-hoc sets. */
    private @Nullable String classpathPath;

    /**
     * Creates shortcut list with the specified defaults and user shortcuts.
     * Look for specified file in these places in this order:
     * <ol>
     * <li>Stream in classpath
     * <li>File of same name in the user's config dir
     * </ol>
     * For each shortcut, user shortcuts have priority, then defaults (for
     * Mac-specific or others).
     *
     * @param classpathRoot
     *            the path to the file on the classpath. Should include a
     *            trailing slash.
     * @param filename
     *            name of file to load
     */
    static PropertiesShortcuts loadBundled(String classpathRoot, String filename) {
        PropertiesShortcuts result = new PropertiesShortcuts();
        result.userFileName = filename;
        result.classpathPath = classpathRoot + filename;
        try {
            result.loadFromClasspath(classpathRoot + filename);
            result.loadFromFile(new File(StaticUtils.getConfigDir(), filename));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load shortcuts properties file", ex);
        }
        return result;
    }

    public void loadFromClasspath(String propertiesFile) throws IOException {
        boolean loaded = false;
        if (Platform.isMacOSX()) {
            String macSpecific = getMacProperties(propertiesFile);
            loaded = loadFromClasspathImpl(macSpecific);
        }
        if (!loaded) {
            loadFromClasspathImpl(propertiesFile);
        }
    }

    /**
     * The properties-file value for a keystroke, parseable by
     * {@link KeyStroke#getKeyStroke(String)}; the empty string (= explicitly
     * unbound) for null.
     */
    public static String toPropertyValue(@Nullable KeyStroke ks) {
        return ks == null ? "" : ks.toString();
    }

    /** Keys of all known shortcutable functions, defaults and overrides. */
    public Set<String> getKeys() {
        Set<String> keys = new TreeSet<>(defaults.keySet());
        keys.addAll(userOverrides.keySet());
        return Collections.unmodifiableSet(keys);
    }

    /**
     * Raw current value of the key ("" = explicitly unbound), or null when
     * the key is unknown.
     */
    public @Nullable String getShortcutValue(String key) {
        String override = userOverrides.get(key);
        return override != null ? override : defaults.get(key);
    }

    /** Raw bundled default of the key, or null when the key is unknown. */
    public @Nullable String getDefaultValue(String key) {
        return defaults.get(key);
    }

    /** Whether the key currently differs from its bundled default. */
    public boolean isModified(String key) {
        return userOverrides.containsKey(key);
    }

    /**
     * Sets the shortcut of the key for this session; null unbinds it
     * explicitly. A value equal to the bundled default removes the override
     * instead. Takes effect in consumers on the next (re)bind; persistent
     * only after {@link #save()}.
     */
    public void setShortcut(String key, @Nullable KeyStroke ks) {
        // Equality of the keystrokes, not of the raw strings: the canonical
        // format ("ctrl pressed D") differs from the hand-written file
        // syntax ("ctrl D") for the same keystroke.
        if (defaults.containsKey(key) && Objects.equals(ks, parse(defaults.get(key)))) {
            userOverrides.remove(key);
        } else {
            userOverrides.put(key, toPropertyValue(ks));
        }
    }

    private static @Nullable KeyStroke parse(@Nullable String value) {
        return value == null || value.isEmpty() ? null : KeyStroke.getKeyStroke(value);
    }

    /** Restores the bundled default of the key for this session. */
    public void clearUserOverride(String key) {
        userOverrides.remove(key);
    }

    /**
     * Writes the current overrides to the user file in the config dir (on
     * macOS the .mac.properties variant, matching the load preference) and
     * notifies the change listeners. Keys at their bundled default are not
     * written, so later default changes reach the user.
     */
    public void save() throws IOException {
        String name = userFileName;
        if (name == null) {
            throw new IllegalStateException("This shortcut set is not backed by a user file");
        }
        if (Platform.isMacOSX()) {
            name = getMacProperties(name);
        }
        File file = new File(StaticUtils.getConfigDir(), name);
        try (BufferedWriter out = Files.newBufferedWriter(file.toPath(), StandardCharsets.ISO_8859_1)) {
            out.write("# Shortcut overrides written by the OmegaT preferences dialog.");
            out.newLine();
            out.write("# Keys absent here follow the application defaults; comments are not preserved.");
            out.newLine();
            for (Map.Entry<String, String> entry : new TreeMap<>(userOverrides).entrySet()) {
                out.write(escapeKey(entry.getKey()) + "=" + entry.getValue());
                out.newLine();
            }
        }
        // Consumers rebind Swing components, so they are notified on the EDT
        // regardless of the calling thread (the preferences dialog saves
        // from a worker).
        SwingUtilities.invokeLater(() -> changeListeners.forEach(Runnable::run));
    }

    /** Escapes the properties-format metacharacters of a key. */
    private static String escapeKey(String key) {
        return key.replace("\\", "\\\\").replace(" ", "\\ ").replace("=", "\\=")
                .replace(":", "\\:").replace("#", "\\#").replace("!", "\\!");
    }

    /**
     * Discards unsaved session changes by reloading the bundled defaults and
     * the user file.
     */
    public void reload() throws IOException {
        String name = userFileName;
        String classpath = classpathPath;
        if (name == null || classpath == null) {
            throw new IllegalStateException("This shortcut set is not backed by a user file");
        }
        defaults.clear();
        userOverrides.clear();
        loadFromClasspath(classpath);
        loadFromFile(new File(StaticUtils.getConfigDir(), name));
    }

    /** Registers a listener notified after {@link #save()}, on the EDT. */
    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(Runnable listener) {
        changeListeners.remove(listener);
    }

    private String getMacProperties(String properties) {
        return properties.replaceAll("\\.properties$", ".mac.properties");
    }

    /**
     * Load the properties file from the classpath
     *
     * @param path
     * @return whether the file was loaded (<code>false</code> if not present,
     *         etc.)
     * @throws IOException
     */
    private boolean loadFromClasspathImpl(String path) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in != null) {
                loadProperties(in, defaults);
                return true;
            }
        }
        return false;
    }

    public void loadFromFile(File file) throws IOException {
        boolean loaded = false;
        if (Platform.isMacOSX()) {
            File macSpecific = new File(getMacProperties(file.getPath()));
            if (macSpecific.isFile()) {
                loadFromFileImpl(macSpecific);
                loaded = true;
            }
        }
        if (!loaded && file.isFile()) {
            loadFromFileImpl(file);
        }
    }

    private void loadFromFileImpl(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            loadProperties(fis, userOverrides);
        }
        // Entries at their default are not overrides; without this, a saved
        // file from an older default set would freeze those keys forever.
        // Compared as keystrokes, so a differently spelled equal value does
        // not count as an override either.
        userOverrides.entrySet().removeIf(e -> defaults.containsKey(e.getKey())
                && Objects.equals(parse(e.getValue()), parse(defaults.get(e.getKey()))));
    }

    private static void loadProperties(InputStream in, Map<String, String> target) throws IOException {
        Properties props = new Properties();
        props.load(in);
        props.forEach((k, v) -> target.put(k.toString(), v.toString()));
    }

    public KeyStroke getKeyStroke(String key) {
        String shortcut = getShortcutValue(key);
        if (shortcut == null) {
            throw new IllegalArgumentException("Keyboard shortcut not defined. Key=" + key);
        }
        KeyStroke result = KeyStroke.getKeyStroke(shortcut);
        if (!shortcut.isEmpty() && result == null) {
            LOGGER.warning("Keyboard shortcut is invalid: " + key + "=" + shortcut);
        }
        return result;
    }

    public void bindKeyStrokes(JMenuBar menu) {
        applyTo(menu.getComponents());
    }

    /**
     * Travel by all submenus for setup shortcuts.
     *
     * @param menu
     *            menu or menu item
     */
    private void applyTo(final Component[] items) {
        for (Component c : items) {
            if (c instanceof JMenuItem) {
                bindKeyStrokes((JMenuItem) c);
            }
        }
    }

    public void bindKeyStrokes(final JMenuItem item) {
        if (item instanceof JMenu) {
            // setAccelerator() is not defined for JMenu.
            applyTo(((JMenu) item).getMenuComponents());
        } else {
            String shortcut = item.getActionCommand();
            if (!StringUtil.isEmpty(shortcut)) {
                try {
                item.setAccelerator(getKeyStroke(shortcut));
                } catch (Exception ex) {
                    // Eat exception silently
                }
            }
        }
    }

    public void bindKeyStrokes(InputMap inputMap, String... keys) {
        for (String key : keys) {
            try {
                KeyStroke keyStroke = getKeyStroke(key);
                if (keyStroke == null) {
                    removeEntry(inputMap, key);
                } else {
                    inputMap.put(keyStroke, key);
                }
            } catch (Exception ex) {
                // Eat exception silently
            }
        }
    }

    private KeyStroke removeEntry(InputMap inputMap, String keyToBeRemoved) {
        KeyStroke removedEntry = null;
        for (KeyStroke ks : inputMap.keys()) {
            String key = (String) inputMap.get(ks);
            if (key.equals(keyToBeRemoved)) {
                inputMap.remove(ks);
                removedEntry = ks;
                break;
            }
        }
        return removedEntry;
    }

    public boolean isEmpty() {
        return defaults.isEmpty() && userOverrides.isEmpty();
    }

    /**
     * For testing purposes
     *
     * @return Unmodifiable merged view of the data held by this instance
     */
    Map<String, String> getData() {
        Map<String, String> merged = new HashMap<>(defaults);
        merged.putAll(userOverrides);
        return Collections.unmodifiableMap(merged);
    }
}
