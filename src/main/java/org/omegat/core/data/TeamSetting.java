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
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.Nullable;
import org.openide.awt.Mnemonics;

import org.omegat.util.OStrings;

/**
 * Descriptor of one per-project setting negotiated with team. Feature code
 * registers descriptor through {@link TeamSettingsRegistry}; core then
 * loads, saves, distributes and questions the setting generically. Raw
 * values are strings; null means "not configured", i.e. default. Values
 * are stored through pluggable {@link Storage}: by default a key in the
 * sidecar file handled by {@link ProjectSettingsStorage}, alternatively a
 * whole configuration file of the project whose canonical content is the
 * raw value.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public final class TeamSetting {

    /**
     * Persistence of one team setting's raw value in a file tree - the
     * project checkout, or through a resolver a repository checkout.
     * Implementations must treat null as "not configured": loading an
     * absent value returns null, saving null removes the stored value.
     */
    public interface Storage {

        /**
         * Raw value carried by the files the resolver yields, null when
         * not configured or unreadable. The resolver maps a path from
         * {@link #pathsUnderRoot} to the file holding it in the inspected
         * tree, or null when the tree does not cover the path.
         */
        @Nullable
        String loadFrom(ProjectProperties config, Function<String, @Nullable File> fileResolver);

        /** Stores raw value in the project checkout; null removes it. */
        void save(ProjectProperties config, @Nullable String raw) throws IOException;

        /**
         * Project-root-relative paths of the files that carry the setting
         * in the team repositories.
         */
        List<String> pathsUnderRoot(ProjectProperties config);

        /** Stored raw value in the project checkout. */
        default @Nullable String load(ProjectProperties config) {
            return loadFrom(config, path -> new File(config.getProjectRootDir(), path));
        }

        /**
         * Whether any of the setting's files exist in the project
         * checkout; false keeps default projects byte-identical to
         * projects of older OmegaT versions.
         */
        default boolean materialized(ProjectProperties config) {
            return pathsUnderRoot(config).stream()
                    .anyMatch(path -> new File(config.getProjectRootDir(), path).isFile());
        }

        /**
         * Whether the raw value is the content of the storage's own
         * file(s), so sharing a null value may delete them in the team
         * repositories. The internal sidecar storage answers false - its
         * file also carries other settings.
         */
        default boolean fileBacked() {
            return true;
        }
    }

    private static Storage sidecarStorage(String key) {
        return new Storage() {
            @Override
            public @Nullable String loadFrom(ProjectProperties config,
                    Function<String, @Nullable File> fileResolver) {
                File file = fileResolver.apply(pathsUnderRoot(config).get(0));
                return file == null ? null : ProjectSettingsStorage.loadFromFile(file, key);
            }

            @Override
            public void save(ProjectProperties config, @Nullable String raw) throws IOException {
                ProjectSettingsStorage.save(config, key, raw);
            }

            @Override
            public List<String> pathsUnderRoot(ProjectProperties config) {
                return List.of(config.getProjectInternalRelative()
                        + ProjectSettingsStorage.FILE_PROJECT_SETTINGS);
            }

            @Override
            public boolean fileBacked() {
                return false;
            }
        };
    }

    private final String key;
    private final String nameKey;
    private final Function<ProjectProperties, @Nullable String> reader;
    private final BiConsumer<ProjectProperties, @Nullable String> applier;
    private final Function<@Nullable String, String> valueDescriber;
    private final UnaryOperator<@Nullable String> normalizer;
    private final Storage storage;

    private TeamSetting(String key, String nameKey, Function<ProjectProperties, @Nullable String> reader,
            BiConsumer<ProjectProperties, @Nullable String> applier,
            Function<@Nullable String, String> valueDescriber, UnaryOperator<@Nullable String> normalizer,
            Storage storage) {
        this.key = key;
        this.nameKey = nameKey;
        this.reader = reader;
        this.applier = applier;
        this.valueDescriber = valueDescriber;
        this.normalizer = normalizer;
        this.storage = storage;
    }

    /**
     * Descriptor with fully custom behaviour, for non-boolean settings.
     *
     * @param key
     *            key in project_settings.properties
     * @param nameKey
     *            resource key of localized display name
     * @param reader
     *            current session value as normalized raw string, null for
     *            default
     * @param applier
     *            applies raw value (null = default) to session; must be an
     *            idempotent setter, it also runs with unchanged values
     * @param valueDescriber
     *            localized description of raw value for dialogs
     * @param normalizer
     *            canonical form of raw value; must map default to null
     */
    public static TeamSetting of(String key, String nameKey,
            Function<ProjectProperties, @Nullable String> reader,
            BiConsumer<ProjectProperties, @Nullable String> applier,
            Function<@Nullable String, String> valueDescriber, UnaryOperator<@Nullable String> normalizer) {
        return new TeamSetting(key, nameKey, reader, applier, valueDescriber, normalizer,
                sidecarStorage(key));
    }

    /**
     * Descriptor of a setting stored in its own configuration file(s)
     * instead of the sidecar. The raw value is the canonical content of the
     * file, null means the file is absent, and sharing the value with the
     * team distributes the file itself - including its removal.
     *
     * @param key
     *            registry key of the setting
     * @param nameKey
     *            resource key of localized display name
     * @param reader
     *            current session value as canonical content, null when the
     *            project carries no specific configuration
     * @param applier
     *            applies canonical content (null = default) to session; must
     *            be an idempotent setter, it also runs with unchanged values
     * @param valueDescriber
     *            localized description of raw value for dialogs
     * @param storage
     *            persistence of the configuration file(s)
     */
    public static TeamSetting ofStoredFile(String key, String nameKey,
            Function<ProjectProperties, @Nullable String> reader,
            BiConsumer<ProjectProperties, @Nullable String> applier,
            Function<@Nullable String, String> valueDescriber, Storage storage) {
        UnaryOperator<@Nullable String> normalizer = raw -> raw == null || raw.isBlank() ? null : raw;
        return new TeamSetting(key, nameKey, reader, applier, valueDescriber, normalizer, storage);
    }

    /**
     * Boolean setting; absent key means given default, only the non-default
     * value materialises in the file, so default projects stay byte-identical
     * to projects of older OmegaT versions.
     */
    public static TeamSetting ofBoolean(String key, String nameKey, boolean defaultValue,
            Predicate<ProjectProperties> getter, BiConsumer<ProjectProperties, Boolean> setter) {
        UnaryOperator<@Nullable String> normalizer = raw -> {
            boolean value = raw == null ? defaultValue : Boolean.parseBoolean(raw.trim());
            return value == defaultValue ? null : Boolean.toString(value);
        };
        return new TeamSetting(key, nameKey,
                config -> getter.test(config) == defaultValue ? null : Boolean.toString(!defaultValue),
                (config, raw) -> setter.accept(config,
                        raw == null ? defaultValue : Boolean.parseBoolean(raw.trim())),
                raw -> OStrings.getString(
                        (raw == null ? defaultValue : Boolean.parseBoolean(raw.trim()))
                                ? "TEAM_SETTING_VALUE_ON"
                                : "TEAM_SETTING_VALUE_OFF"),
                normalizer, sidecarStorage(key));
    }

    /** Registry key of the setting. */
    public String getKey() {
        return key;
    }

    /** Stored normalized raw value, null when not configured. */
    public @Nullable String loadStored(ProjectProperties config) {
        return normalize(storage.load(config));
    }

    /**
     * Normalized raw value carried by the files the resolver yields, e.g.
     * a repository checkout - see {@link Storage#loadFrom}.
     */
    public @Nullable String loadStoredFrom(ProjectProperties config,
            Function<String, @Nullable File> fileResolver) {
        return normalize(storage.loadFrom(config, fileResolver));
    }

    /** Stores raw value; null removes the stored value. */
    public void saveStored(ProjectProperties config, @Nullable String raw) throws IOException {
        storage.save(config, normalize(raw));
    }

    /**
     * Project-root-relative paths of the files that carry the setting in
     * the team repositories.
     */
    public List<String> storagePaths(ProjectProperties config) {
        return storage.pathsUnderRoot(config);
    }

    /** Whether the setting's file(s) exist in the project checkout. */
    public boolean storageMaterialized(ProjectProperties config) {
        return storage.materialized(config);
    }

    /**
     * Whether the raw value is the content of own configuration file(s)
     * ({@link #ofStoredFile}) instead of a key in the shared sidecar file.
     * Only for such a setting may sharing a null value delete files in the
     * team repositories - the sidecar file also carries other settings.
     */
    public boolean isFileBacked() {
        return storage.fileBacked();
    }

    /**
     * Localized display name for dialogs. Mnemonic markers are stripped, so
     * existing menu or checkbox label keys work as name key.
     */
    public String getDisplayName() {
        return Mnemonics.removeMnemonics(OStrings.getString(nameKey));
    }

    /** Current session value as normalized raw string, null for default. */
    public @Nullable String read(ProjectProperties config) {
        return normalize(reader.apply(config));
    }

    /** Applies raw value (null = default) to session. */
    public void apply(ProjectProperties config, @Nullable String raw) {
        applier.accept(config, normalize(raw));
    }

    /** Localized description of raw value for dialogs. */
    public String describe(@Nullable String raw) {
        return valueDescriber.apply(raw);
    }

    /** Canonical form of raw value; default maps to null. */
    public @Nullable String normalize(@Nullable String raw) {
        return normalizer.apply(raw);
    }
}
