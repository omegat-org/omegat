package org.omegat.cli;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.RealProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;
import org.omegat.util.TMXWriter2;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.List;

@CommandLine.Command(name = "pseudo")
public class PseudoTranslateCommand implements Runnable{

    @CommandLine.ParentCommand
    private LegacyParameters legacyParameters;

    @CommandLine.Mixin
    private Parameters params;

    @Override
    public void run() {
        legacyParameters.initialize();
        params.initialize();
        int status;
        try {
            status = runCreatePseudoTranslateTMX();
            if (status != 0) {
                System.exit(status);
            }
        } catch (Exception e) {}
    }

    /**
     * Execute in console mode for translate.
     */
    int runCreatePseudoTranslateTMX() throws Exception {
        Log.logInfoRB("CONSOLE_PSEUDO_TRANSLATION_MODE");

        System.out.println(OStrings.getString("CONSOLE_INITIALIZING"));
        Core.initializeConsole();

        RealProject p = Common.selectProjectConsoleMode(true, params);

        Common.validateTagsConsoleMode(params);

        System.out.println(OStrings.getString("CONSOLE_CREATE_PSEUDOTMX"));

        ProjectProperties config = p.getProjectProperties();
        List<SourceTextEntry> entries = p.getAllEntries();
        String pseudoTranslateTMXFilename = legacyParameters.pseudoTranslateTmxPath;
        String pseudoTranslateType = legacyParameters.pseudoTranslateTypeName;

        String fname;
        if (!StringUtil.isEmpty(pseudoTranslateTMXFilename)) {
            if (!pseudoTranslateTMXFilename.endsWith(OConsts.TMX_EXTENSION)) {
                fname = pseudoTranslateTMXFilename + "." + OConsts.TMX_EXTENSION;
            } else {
                fname = pseudoTranslateTMXFilename;
            }
        } else {
            fname = "";
        }

        // Write OmegaT-project-compatible TMX:
        try (TMXWriter2 wr = new TMXWriter2(new File(fname), config.getSourceLanguage(),
                config.getTargetLanguage(), config.isSentenceSegmentingEnabled(), false, false)) {
            for (SourceTextEntry ste : entries) {
                if ("equal".equalsIgnoreCase(pseudoTranslateType)) {
                    wr.writeEntry(ste.getSrcText(), ste.getSrcText(), null, null, 0, null, 0, null);
                } else if ("empty".equalsIgnoreCase(pseudoTranslateType)) {
                    wr.writeEntry(ste.getSrcText(), "", null, null, 0, null, 0, null);
                }
            }
        } catch (IOException e) {
            Log.logErrorRB("CT_ERROR_CREATING_TMX");
            Log.log(e);
            throw new IOException(OStrings.getString("CT_ERROR_CREATING_TMX") + "\n" + e.getMessage());
        }
        p.closeProject();
        System.out.println(OStrings.getString("CONSOLE_FINISHED"));
        return 0;
    }

}
