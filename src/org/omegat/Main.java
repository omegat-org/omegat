/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Martin Fleurke, Alex Buloichik, Didier Briel
               2012 Aaron Madlon-Kay
               2013 Kyle Katarn, Aaron Madlon-Kay
               2014 Alex Buloichik
               2018 Enrique Estevez Fernandez
               2022-2025 Hiroshi Miura
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

package org.omegat;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.JLanguageTool;
import org.omegat.cli.AlignCommand;
import org.omegat.cli.Parameters;
import org.omegat.cli.StartCommand;
import org.omegat.cli.StatsCommand;
import org.omegat.cli.TeamCommand;
import org.omegat.cli.TranslateCommand;
import picocli.CommandLine;

import org.omegat.core.Core;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.languagetools.LanguageClassBroker;
import org.omegat.languagetools.LanguageDataBroker;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;

/**
 * The main OmegaT class, used to launch the program.
 *
 * @author Keith Godfrey
 * @author Martin Fleurke
 * @author Alex Buloichik
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 * @author Kyle Katarn
 * @author Hiroshi Miura
 */
public final class Main {

    private Main() {
    }

    private static Parameters parameters;

    public static void main(String[] args) {
        // construct parser and execute
        parameters = new Parameters();
        CommandLine commandLine = new CommandLine(parameters);
        commandLine.addSubcommand("team", new TeamCommand());
        commandLine.addSubcommand("align", new AlignCommand(parameters));
        commandLine.addSubcommand("stats", new StatsCommand(parameters));
        commandLine.addSubcommand("translate", new TranslateCommand(parameters));
        commandLine.addSubcommand("start", new StartCommand(parameters));
        commandLine.addSubcommand("help", new CommandLine.HelpCommand());
        ResourceBundle resourceBundle = ResourceBundle.getBundle("org.omegat.cli.Parameters");
        commandLine.setResourceBundle(resourceBundle);
        commandLine.setExecutionStrategy(new CommandLine.RunLast());

        // Workaround for Java 17 or later support of JAXB.
        // See https://sourceforge.net/p/omegat/feature-requests/1682/#12c5
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");

        System.setProperty("http.agent", OStrings.getDisplayNameAndVersion());

        // Do migration and load various settings. The order is important!
        Preferences.init();
        // broker should be loaded before module loading
        JLanguageTool.setClassBrokerBroker(new LanguageClassBroker());
        JLanguageTool.setDataBroker(new LanguageDataBroker());
        PluginUtils.loadPlugins(null);
        FilterMaster.setFilterClasses(PluginUtils.getFilterClasses());
        Preferences.initFilters();
        Preferences.initSegmentation();

        commandLine.execute(args);
    }

    public static void restartGUI(String projectDir) {
        // Check we have `java` command in java.home
        Path javaBin = Paths.get(System.getProperty("java.home")).resolve("bin/java");
        List<String> command = new ArrayList<>();
        if (javaBin.toFile().exists()) {
            // Build command: java -cp ... org.omegat.Main
            command.add(javaBin.toString());
            RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
            command.addAll(runtimeMxBean.getInputArguments()); // JVM args
            command.add("-cp");
            command.add(runtimeMxBean.getClassPath());
            command.add(Main.class.getName());
            command.addAll(parameters.constructGuiArgs());
        } else {
            // assumes jpackage
            var installDir = StaticUtils.installDir();
            if (installDir == null) {
                return;
            } else {
                javaBin = Paths.get(StaticUtils.installDir()).getParent().resolve("bin/OmegaT");
                if (!javaBin.toFile().exists()) {
                    // abort restart
                    Core.getMainWindow().displayWarningRB("LOG_RESTART_FAILED_NOT_FOUND");
                    return;
                }
                command.add(javaBin.toString());
                command.addAll(parameters.constructGuiArgs());
            }
        }
        if (projectDir != null) {
            command.add(projectDir);
        }
        // Now ready to restart.
        Log.log("===         Restart OmegaT           ===");
        ProcessBuilder builder = new ProcessBuilder(command);
        try {
            builder.start();
            System.exit(0);
        } catch (IOException e) {
            Log.log(e);
            System.exit(1);
        }
    }

}
