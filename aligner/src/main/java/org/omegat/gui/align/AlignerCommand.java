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
package org.omegat.gui.align;

import org.omegat.cli.CommandCommon;
import org.omegat.cli.CommonParameters;
import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.RealProject;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.util.FileUtil;
import org.omegat.util.Log;
import org.omegat.util.TMXWriter2;
import org.omegat.util.gui.UIDesignManager;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * CLI subcommand to trigger the Aligner feature provided by the aligner module.
 */
@Command(name = "aligner", description = "Launch the Aligner")
public class AlignerCommand implements Callable<Integer> {

    @CommandLine.Mixin
    CommonParameters params;

    @CommandLine.Parameters(index = "0", paramLabel = "<project>", description = "The project folder to align.")
    String project;

    @Option(names = { "-G", "--gui" }, negatable = true, description = "Launch the aligner window")
    boolean gui = false;

    @Override
    public Integer call() throws Exception {
        Core.initializeConsole();
        int status;
        if (gui) {
           status = runGUIAligner();

        } else {
            status = runConsoleAlign();
        }
        return status;
    }

    int runConsoleAlign() throws Exception {
        CommandCommon.showStartUpLogInfo();
        CommandCommon.logLevelInitialize(params);
        Log.logInfoRB("CONSOLE_ALIGNMENT_MODE");

        CommandCommon.initializeApp();
        Core.initializeConsole();

        CommandCommon.parseCommonParams(params);
        RealProject p = CommandCommon.selectProjectConsoleMode(true, params);
        CommandCommon.validateTagsConsoleMode(params);

        String tmxFile = p.getProjectProperties().getProjectInternal() + "align.tmx";
        ProjectProperties config = p.getProjectProperties();
        boolean alt = !config.isSupportDefaultTranslations();
        try (TMXWriter2 wr = new TMXWriter2(new File(tmxFile), config.getSourceLanguage(),
                config.getTargetLanguage(), config.isSentenceSegmentingEnabled(), alt, alt)) {
            wr.writeEntries(p.align(config, new File(FileUtil.expandTildeHomeDir(project))),
                    alt);
        }
        p.closeProject();
        Log.logInfoRB("CONSOLE_FINISHED");
        return 0;
    }

    int runGUIAligner() {
        if (params == null) {
            return 1;
        }
        CommandCommon.showStartUpLogInfo();
        CommandCommon.logLevelInitialize(params);
        String dir = project;
        try {
            UIDesignManager.initialize();
        } catch (IOException e) {
            Log.log(e);
            return 1;
        }
        Core.setFilterMaster(new FilterMaster(FilterMaster.createDefaultFiltersConfig()));
        Core.setSegmenter(new Segmenter(SRX.getDefault()));
        AlignerModule.alignerShow(dir);
        return 0;
    }
}
