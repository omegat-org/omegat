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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.jspecify.annotations.Nullable;

import org.omegat.util.Log;

/**
 * Storage of optional per-project settings in
 * {@code omegat/project_settings.properties}. The settings live in their own
 * file instead of {@code omegat.project} on purpose: the project file parser
 * of released OmegaT versions rejects unknown elements, so a new element
 * there would make the project unreadable for every team member on an older
 * version, while an extra file in the {@code omegat} folder is ignored.
 *
 * The file is written deterministically (sorted keys, no timestamp comment),
 * so distributing an unchanged file to a team repository stays recognisable
 * as a byte-identical no-op.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public final class ProjectSettingsStorage {

    public static final String FILE_PROJECT_SETTINGS = "project_settings.properties";

    private ProjectSettingsStorage() {
    }

    /** The settings file of the given project. */
    public static File getFile(ProjectProperties config) {
        return new File(config.getProjectInternal(), FILE_PROJECT_SETTINGS);
    }

    /**
     * The stored raw value of given key: null when the file or the key is
     * absent or the file is unreadable, so callers can distinguish "not
     * configured" from an explicit value.
     */
    public static @Nullable String load(ProjectProperties config, String key) {
        File file = getFile(config);
        if (!file.isFile()) {
            return null;
        }
        Properties props = new Properties();
        try (Reader in = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            props.load(in);
        } catch (IOException ex) {
            Log.logErrorRB(ex, "TEAM_SETTING_APPLY_ERROR");
            return null;
        }
        String value = props.getProperty(key);
        return value == null ? null : value.trim();
    }

    /**
     * Store the raw value under given key (null removes the key), keeping
     * any other keys of the file so settings can share it. Removing a key
     * from an absent file is a no-op, so it never materialises the file.
     */
    public static void save(ProjectProperties config, String key, @Nullable String value)
            throws IOException {
        File file = getFile(config);
        if (value == null && !file.isFile()) {
            return;
        }
        Map<String, String> entries = new TreeMap<>();
        if (file.isFile()) {
            Properties existing = new Properties();
            try (Reader in = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                existing.load(in);
            }
            existing.stringPropertyNames().forEach(k -> entries.put(k, existing.getProperty(k)));
        }
        if (value == null) {
            entries.remove(key);
        } else {
            entries.put(key, value);
        }
        File dir = file.getParentFile();
        if (dir != null && !dir.isDirectory() && !dir.mkdirs()) {
            throw new IOException("Cannot create " + dir);
        }
        try (Writer out = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            for (Map.Entry<String, String> e : entries.entrySet()) {
                out.write(escape(e.getKey(), true) + "=" + escape(e.getValue(), false) + "\n");
            }
        }
    }

    /**
     * Escape a key or value the way {@code Properties.store} would, minus
     * the Latin-1 escaping (the file is read and written as UTF-8), so
     * foreign keys of other settings survive the load/save round trip
     * unchanged.
     */
    private static String escape(String s, boolean isKey) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
            case '\\':
                sb.append("\\\\");
                break;
            case '\t':
                sb.append("\\t");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\r':
                sb.append("\\r");
                break;
            case '\f':
                sb.append("\\f");
                break;
            case '=':
            case ':':
            case '#':
            case '!':
                sb.append('\\').append(c);
                break;
            case ' ':
                if (isKey || i == 0) {
                    sb.append('\\');
                }
                sb.append(c);
                break;
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
