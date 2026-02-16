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

import org.omegat.CLIParameters;
import org.omegat.cli.BaseSubCommand;
import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.RealProject;
import org.omegat.util.FileUtil;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;
import org.omegat.util.TMXWriter2;

import java.io.File;

/**
 * CLI subcommand to trigger the Aligner feature provided by the aligner module.
 */
public class AlignCommand extends BaseSubCommand {

    @Override
    public boolean isProjectRequired() {
        return true;
    }

    @Override
    public Integer call() throws Exception {
        RealProject p = (RealProject) Core.getProject();

        String tmxFile = p.getProjectProperties().getProjectInternal() + "align.tmx";
        ProjectProperties config = p.getProjectProperties();

        String alignDir = getParam(CLIParameters.ALIGNDIR);
        if (alignDir == null) {
            System.out.println(OStrings.getString("CONSOLE_TRANSLATED_FILES_LOC_UNDEFINED"));
            return 1;
        }
        System.out.println(StringUtil.format(OStrings.getString("CONSOLE_ALIGN_AGAINST"), alignDir));

        boolean alt = !config.isSupportDefaultTranslations();
        try (TMXWriter2 wr = new TMXWriter2(new File(tmxFile), config.getSourceLanguage(),
                config.getTargetLanguage(), config.isSentenceSegmentingEnabled(), alt, alt)) {
            wr.writeEntries(p.align(config, new File(FileUtil.expandTildeHomeDir(alignDir))), alt);
        }
        p.closeProject();
        return 0;
    }
}
