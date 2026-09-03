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

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.Nullable;
import org.openide.awt.Mnemonics;

import org.omegat.util.OStrings;

/**
 * Descriptor of one per-project setting negotiated with team, stored in
 * sidecar file handled by {@link ProjectSettingsStorage}. Feature code
 * registers descriptor through {@link TeamSettingsRegistry}; core then
 * loads, saves, distributes and questions the setting generically. Raw
 * values are strings; null means "not configured", i.e. default.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public final class TeamSetting {

    private final String key;
    private final String nameKey;
    private final Function<ProjectProperties, @Nullable String> reader;
    private final BiConsumer<ProjectProperties, @Nullable String> applier;
    private final Function<@Nullable String, String> valueDescriber;
    private final UnaryOperator<@Nullable String> normalizer;

    private TeamSetting(String key, String nameKey, Function<ProjectProperties, @Nullable String> reader,
            BiConsumer<ProjectProperties, @Nullable String> applier,
            Function<@Nullable String, String> valueDescriber, UnaryOperator<@Nullable String> normalizer) {
        this.key = key;
        this.nameKey = nameKey;
        this.reader = reader;
        this.applier = applier;
        this.valueDescriber = valueDescriber;
        this.normalizer = normalizer;
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
        return new TeamSetting(key, nameKey, reader, applier, valueDescriber, normalizer);
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
                normalizer);
    }

    /** Key in project_settings.properties. */
    public String getKey() {
        return key;
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
