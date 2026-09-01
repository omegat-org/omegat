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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import gen.core.filters.Filters;
import org.omegat.core.Core;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.SRXManager;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * The built-in file-backed team settings: the project's file filter
 * configuration ({@code omegat/filters.xml}) and segmentation rules
 * ({@code omegat/segmentation.srx}, legacy {@code segmentation.conf}). A
 * team sync used to overwrite local changes to these files silently and
 * OmegaT never committed them itself; registered as {@link TeamSetting},
 * the generic negotiation asks instead and offers to distribute a change
 * to the team. The raw value is the canonical serialized content, so
 * differently formatted files with equal rules do not count as diverged.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public final class TeamSettingFiles {

    /** Registry key of the file filter configuration setting. */
    public static final String FILTERS_KEY = "filters";

    /** Registry key of the segmentation rules setting. */
    public static final String SEGMENTATION_KEY = "segmentation";

    /** File filter configuration as a team setting. */
    public static final TeamSetting FILTERS = TeamSetting.ofStoredFile(FILTERS_KEY,
            "TEAM_SETTING_NAME_FILTERS", TeamSettingFiles::readSessionFilters,
            TeamSettingFiles::applySessionFilters, TeamSettingFiles::describeFile,
            new FiltersStorage());

    /** Segmentation rules as a team setting. */
    public static final TeamSetting SEGMENTATION = TeamSetting.ofStoredFile(SEGMENTATION_KEY,
            "TEAM_SETTING_NAME_SEGMENTATION", TeamSettingFiles::readSessionSegmentation,
            TeamSettingFiles::applySessionSegmentation, TeamSettingFiles::describeFile,
            new SegmentationStorage());

    private static final Object REGISTER_LOCK = new Object();

    private TeamSettingFiles() {
    }

    /** Registers both settings when absent; safe under concurrent calls. */
    public static void ensureRegistered() {
        synchronized (REGISTER_LOCK) {
            for (TeamSetting setting : List.of(FILTERS, SEGMENTATION)) {
                if (TeamSettingsRegistry.byKey(setting.getKey()) == null) {
                    TeamSettingsRegistry.register(setting);
                }
            }
        }
    }

    private static @Nullable String readSessionFilters(ProjectProperties config) {
        Filters filters = config.getProjectFilters();
        if (filters == null) {
            return null;
        }
        try {
            return FilterMaster.writeConfigToString(canonical(filters));
        } catch (IOException ex) {
            Log.logErrorRB(ex, "TEAM_SETTING_APPLY_ERROR");
            return null;
        }
    }

    /**
     * The canonical form includes the completion with this installation's
     * available filters that loading a file applies anyway: without it, a
     * configuration from a team repository or an older OmegaT version
     * would differ from every locally (re)written file, reading as a
     * divergence on each open although nobody changed anything.
     */
    private static Filters canonical(Filters filters) {
        return FilterMaster.completeWithAvailableFilters(filters);
    }

    private static void applySessionFilters(ProjectProperties config, @Nullable String raw) {
        if (raw == null) {
            config.setProjectFilters(null);
        } else {
            try {
                config.setProjectFilters(canonical(FilterMaster.loadConfigFromString(raw)));
            } catch (IOException ex) {
                // Unparseable content: keep the current session
                // configuration instead of silently falling back to global
                // defaults.
                Log.logErrorRB(ex, "TEAM_SETTING_APPLY_ERROR");
                return;
            }
        }
        // Answering the divergence question must take effect like flipping
        // a value setting does; entries loaded before the swap keep their
        // parse until the next reload, as with any mid-session change.
        if (appliesToLoadedProject(config)) {
            Core.setFilterMaster(new FilterMaster(
                    Optional.ofNullable(config.getProjectFilters()).orElse(Preferences.getFilters())));
        }
    }

    private static @Nullable String readSessionSegmentation(ProjectProperties config) {
        SRX srx = config.getProjectSRX();
        if (srx == null) {
            return null;
        }
        try {
            return SRXManager.writeToString(srx);
        } catch (IOException ex) {
            Log.logErrorRB(ex, "TEAM_SETTING_APPLY_ERROR");
            return null;
        }
    }

    private static void applySessionSegmentation(ProjectProperties config, @Nullable String raw) {
        if (raw == null) {
            config.setProjectSRX(null);
        } else {
            try {
                config.setProjectSRX(SRXManager.loadSrxFromString(raw));
            } catch (IOException ex) {
                Log.logErrorRB(ex, "TEAM_SETTING_APPLY_ERROR");
                return;
            }
        }
        if (appliesToLoadedProject(config)) {
            Core.setSegmenter(new Segmenter(
                    Optional.ofNullable(config.getProjectSRX()).orElse(Preferences.getSRX())));
        }
    }

    /**
     * Whether the given properties belong to the loaded project, so the
     * apply must also rewire the live filter master or segmenter. False
     * during a project load - the load wires them itself right after the
     * settings resolution - and for detached copies like the properties
     * dialog working set.
     */
    private static boolean appliesToLoadedProject(ProjectProperties config) {
        return Core.getProject().isProjectLoaded()
                && Core.getProject().getProjectProperties() == config;
    }

    /**
     * Dialogs cannot show whole files, so the description names the state
     * and fingerprints the content, letting the user tell two project
     * versions apart.
     */
    private static String describeFile(@Nullable String raw) {
        if (raw == null) {
            return OStrings.getString("TEAM_SETTING_VALUE_FILE_NONE");
        }
        return OStrings.getString("TEAM_SETTING_VALUE_FILE_CUSTOM", fingerprint(raw));
    }

    static String fingerprint(String content) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-1")
                    .digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02x", hash[i]));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static final class FiltersStorage implements TeamSetting.Storage {

        @Override
        public @Nullable String loadFrom(ProjectProperties config,
                Function<String, @Nullable File> fileResolver) {
            File file = fileResolver.apply(pathsUnderRoot(config).get(0));
            if (file == null || !file.isFile()) {
                return null;
            }
            try {
                String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                if (content.startsWith("\uFEFF")) {
                    // the byte-stream loader accepted a BOM, so must we
                    content = content.substring(1);
                }
                return FilterMaster
                        .writeConfigToString(canonical(FilterMaster.loadConfigFromString(content)));
            } catch (IOException ex) {
                Log.logErrorRB(ex, "TEAM_SETTING_APPLY_ERROR");
                return null;
            }
        }

        @Override
        public void save(ProjectProperties config, @Nullable String raw) throws IOException {
            FilterMaster.saveConfig(
                    raw == null ? null : canonical(FilterMaster.loadConfigFromString(raw)),
                    new File(config.getProjectInternal(), FilterMaster.FILE_FILTERS));
        }

        @Override
        public List<String> pathsUnderRoot(ProjectProperties config) {
            return List.of(config.getProjectInternalRelative() + FilterMaster.FILE_FILTERS);
        }
    }

    private static final class SegmentationStorage implements TeamSetting.Storage {

        @Override
        public @Nullable String loadFrom(ProjectProperties config,
                Function<String, @Nullable File> fileResolver) {
            List<String> paths = pathsUnderRoot(config);
            File srxFile = fileResolver.apply(paths.get(0));
            File confFile = fileResolver.apply(paths.get(1));
            SRX srx;
            if (srxFile != null && srxFile.isFile()) {
                srx = SRXManager.loadSrxFile(srxFile.toURI());
            } else if (confFile != null && confFile.isFile()) {
                srx = SRXManager.loadConfFileNoMigrate(confFile);
            } else {
                return null;
            }
            if (srx == null) {
                return null;
            }
            try {
                return SRXManager.writeToString(srx);
            } catch (IOException ex) {
                Log.logErrorRB(ex, "TEAM_SETTING_APPLY_ERROR");
                return null;
            }
        }

        @Override
        public void save(ProjectProperties config, @Nullable String raw) throws IOException {
            SRXManager.saveToSrx(raw == null ? null : SRXManager.loadSrxFromString(raw),
                    new File(config.getProjectInternal()));
        }

        @Override
        public List<String> pathsUnderRoot(ProjectProperties config) {
            // Both formats, srx first: sharing mirrors the sharer's
            // checkout, which after the srx migration no longer holds a
            // conf file, so the repository copy of the legacy file goes
            // away with it.
            return List.of(config.getProjectInternalRelative() + SRXManager.SRX_SENTSEG,
                    config.getProjectInternalRelative() + SRXManager.CONF_SENTSEG);
        }
    }
}
