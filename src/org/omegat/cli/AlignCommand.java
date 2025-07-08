/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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

package org.omegat.cli;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.RealProject;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.FileUtil;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;
import org.omegat.util.TMXWriter2;
import org.omegat.util.gui.UIDesignManager;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@Command(name = "align")
public class AlignCommand implements Runnable {

    @CommandLine.ParentCommand
    private LegacyParameters legacyParams;

    @CommandLine.Mixin
    private Parameters params;

    @CommandLine.Parameters(index = "0", paramLabel = "<project>", defaultValue = Option.NULL_VALUE)
    String project;

    @Option(names = { "-G", "--gui" }, versionHelp = true)
    boolean startGUI;

    @Override
    public void run() {
        legacyParams.initialize();
        params.setProjectLocation(Objects.requireNonNullElse(project, "."));
        params.initialize();
        int status;
        try {
            if (startGUI) {
                status = runGUIAligner();
            } else {
                status = runConsoleAlign();
            }
            if (status != 0) {
                System.exit(status);
            }
        } catch (Exception e) {
            System.err.println("Failed to align.");
            System.exit(1);
        }

    }

    int runConsoleAlign() throws Exception {
        Log.logInfoRB("CONSOLE_ALIGNMENT_MODE");

        if (params.projectLocation == null) {
            System.out.println(OStrings.getString("PP_ERROR_UNABLE_TO_READ_PROJECT_FILE"));
            return 1;
        }

        if (legacyParams.alignDirPath == null) {
            System.out.println(OStrings.getString("CONSOLE_TRANSLATED_FILES_LOC_UNDEFINED"));
            return 1;
        }

        System.out.println(OStrings.getString("CONSOLE_INITIALIZING"));
        Core.initializeConsole();
        RealProject p = Common.selectProjectConsoleMode(true, params);

        Common.validateTagsConsoleMode(params);

        System.out
                .println(StringUtil.format(OStrings.getString("CONSOLE_ALIGN_AGAINST"), legacyParams.alignDirPath));

        String tmxFile = p.getProjectProperties().getProjectInternal() + "align.tmx";
        ProjectProperties config = p.getProjectProperties();
        boolean alt = !config.isSupportDefaultTranslations();
        try (TMXWriter2 wr = new TMXWriter2(new File(tmxFile), config.getSourceLanguage(),
                config.getTargetLanguage(), config.isSentenceSegmentingEnabled(), alt, alt)) {
            wr.writeEntries(p.align(config, new File(FileUtil.expandTildeHomeDir(legacyParams.alignDirPath))), alt);
        }
        p.closeProject();
        System.out.println(OStrings.getString("CONSOLE_FINISHED"));
        return 0;
    }

    int runGUIAligner() {
        String dir = params.projectLocation;
        try {
            UIDesignManager.initialize();
        } catch (IOException e) {
            Log.log(e);
            return 1;
        }
        Core.setFilterMaster(new FilterMaster(FilterMaster.createDefaultFiltersConfig()));
        Core.setSegmenter(new Segmenter(SRX.getDefault()));
        try {
            ClassLoader cl = PluginUtils.getClassLoader(PluginUtils.PluginType.BASE);
            if (cl == null) {
                return 1;
            }
            Class<?> alignClass = cl.loadClass("org.omegat.gui.align.AlignerModule");
            Method method = alignClass.getMethod("showAligner", String.class);
            method.invoke(null, dir);
            return 0;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                 | InvocationTargetException e) {
            Log.log(e);
            return 1;
        }
    }
}
