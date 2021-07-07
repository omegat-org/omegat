/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.team2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;

import org.eclipse.jgit.api.Git;
import org.omegat.CLIParameters;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.gui.glossary.GlossaryManager;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.StringUtil;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.wc.SVNClientManager;

/**
 * A utility class implementing useful tools related to team projects. Intended
 * mostly for CLI use.
 *
 * @author Aaron Madlon-Kay
 */
public final class TeamTool {

    private TeamTool() {
    }

    public static final String COMMAND_INIT = "init";

    /**
     * Utility function to create a minimal project to serve as a base for a
     * team project. Will add/stage everything if invoked on a path already
     * containing a git working tree or svn checkout.
     *
     * @param dir
     *            Directory in which to create team project
     * @param srcLang
     *            Source language
     * @param trgLang
     *            Target language
     * @throws Exception
     *             If specified dir is not a directory, is not writeable, etc.
     */
    public static void initTeamProject(File dir, String srcLang, String trgLang) throws Exception {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Specified dir is not a directory: " + dir.getPath());
        }
        if (!dir.canWrite()) {
            throw new IOException("Specified dir is not writeable: " + dir.getPath());
        }

        // Create project properties
        ProjectProperties props = new ProjectProperties(dir);
        props.setSourceLanguage(srcLang);
        props.setTargetLanguage(trgLang);

        // Set default tokenizers
        props.setSourceTokenizer(PluginUtils.getTokenizerClassForLanguage(new Language(srcLang)));
        props.setTargetTokenizer(PluginUtils.getTokenizerClassForLanguage(new Language(trgLang)));

        // Create project internal directories
        props.autocreateDirectories();
        // Create version-controlled glossary file
        GlossaryManager.createNewWritableGlossaryFile(props.getWritableGlossaryFile().getAsFile());

        ProjectFileStorage.writeProjectFile(props);

        // Create empty project TM
        new ProjectTMX(props.getSourceLanguage(), props.getTargetLanguage(), props.isSentenceSegmentingEnabled(), null,
                null).save(props, new File(props.getProjectInternal(), OConsts.STATUS_EXTENSION).getPath(), false);

        // If the supplied dir is under version control, add everything we made
        // and set EOL handling correctly for cross-platform work
        if (new File(dir, ".svn").isDirectory()) {
            SVNClientManager mgr = SVNClientManager.newInstance();
            mgr.getWCClient().doSetProperty(dir, "svn:auto-props",
                    SVNPropertyValue.create("*.txt = svn:eol-style=native\n*.tmx = svn:eol-style=native\n"), false,
                    SVNDepth.EMPTY, null, null);
            mgr.getWCClient().doAdd(dir.listFiles(f -> !f.getName().startsWith(".")), false, false, true,
                    SVNDepth.fromRecurse(true), false, false, false, true);
        } else if (new File(dir, ".git").isDirectory()) {
            try (BufferedWriter writer = Files.newBufferedWriter(new File(dir, ".gitattributes").toPath())) {
                writer.write("* text=auto\n");
                writer.write("*.tmx text\n");
                writer.write("*.txt text\n");
            }
            Git.open(dir).add().addFilepattern(".").call();
        }

        System.out.println(StringUtil.format(OStrings.getString("TEAM_TOOL_INIT_COMPLETE"), srcLang, trgLang));
    }

    public static void showHelp() {
        System.out.println(StringUtil.format(OStrings.getString("TEAM_TOOL_HELP"), OStrings.getNameAndVersion()));
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            showHelp();
            System.exit(1);
        }
        if (Arrays.asList(CLIParameters.HELP, CLIParameters.HELP_SHORT).contains(args[0])) {
            showHelp();
            System.exit(0);
        }

        Log.setLevel(Level.WARNING);

        try {
            Preferences.init();
            PluginUtils.loadPlugins(Collections.emptyMap());
            if (COMMAND_INIT.equals(args[0]) && args.length == 3) {
                initTeamProject(new File("").getAbsoluteFile(), args[1], args[2]);
                System.exit(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        showHelp();
        System.exit(1);
    }
}
