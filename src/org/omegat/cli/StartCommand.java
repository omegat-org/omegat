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
import org.omegat.core.CoreEvents;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.main.ProjectUICommands;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Platform;
import org.omegat.util.RuntimePreferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.OSXIntegration;
import picocli.CommandLine;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Toolkit;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Start sub-command entry.
 */
@CommandLine.Command(name = "start")
public class StartCommand implements Callable<Integer> {

    @CommandLine.ParentCommand
    LegacyParameters legacyParams;

    @CommandLine.Mixin
    Parameters params;

    @CommandLine.Option(names = { "-h", "--help" }, usageHelp = true)
    boolean usageHelpRequested;

    @CommandLine.Parameters(index = "0", paramLabel = "<project>", defaultValue = CommandLine.Option.NULL_VALUE)
    String project;

    public StartCommand() {
    }

    @Override
    public Integer call() {
        if (legacyParams == null || params == null) {
            return 1;
        }
        legacyParams.initialize();
        params.setProjectLocation(Objects.requireNonNullElse(project, "."));
        params.initialize();
        return runGUI();
    }

    /**
     * Execute standard GUI.
     */
    int runGUI() {
        if (params != null && params.noTeam) {
            RuntimePreferences.setNoTeam();
        }
        UIManager.put("ClassLoader", PluginUtils.getClassLoader(PluginUtils.PluginType.THEME));

        // macOS-specific - they must be set BEFORE any GUI calls
        if (Platform.isMacOSX()) {
            OSXIntegration.init();
        }

        // Set X11 application class name to make some desktop user interfaces
        // (like Gnome Shell) recognize OmegaT
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Class<?> cls = toolkit.getClass();
            if (cls.getName().equals("sun.awt.X11.XToolkit")) {
                try {
                    Field field = cls.getDeclaredField("awtAppClassName");
                    if (field.trySetAccessible()) {
                        field.set(toolkit, "OmegaT");
                    }
                } catch (NoSuchFieldException | IllegalAccessException ex) {
                    Log.log(ex);
                }
            }

        System.setProperty("swing.aatext", "true");
        try {
            Core.initializeGUI();
        } catch (Throwable ex) {
            Log.log(ex);
            showError(ex);
            return 1;
        }

        if (!Core.getPluginsLoadingErrors().isEmpty()) {
            String err = String.join("\n", Core.getPluginsLoadingErrors());
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), err,
                    OStrings.getString("STARTUP_ERRORBOX_TITLE"), JOptionPane.ERROR_MESSAGE);
        }

        if (params != null && params.alternateFilenameFrom != null) {
            RuntimePreferences.setAlternateFilenames(params.alternateFilenameFrom,
                    params.alternateFilenameTo);
        }

        CoreEvents.fireApplicationStartup();

        SwingUtilities.invokeLater(() -> {
            // setVisible can't be executed directly, because we need to
            // call all application startup listeners for initialize UI
            Core.getMainWindow().getApplicationFrame().setVisible(true);
            //
            if (params != null) {
                if (isProjectRemote(params.projectLocation)) {
                    ProjectUICommands.projectRemote(params.projectLocation);
                } else if (params.projectLocation != null) {
                    File targetDir = Paths.get(params.projectLocation).toFile();
                    File targetFile = Paths.get(params.projectLocation).resolve("omegat.project").toFile();
                    if (targetDir.isDirectory() && targetFile.exists()) {
                        ProjectUICommands.projectOpen(targetDir);
                    }
                }
            }
        });
        return 0;
    }

    private boolean isProjectRemote(String project) {
        return project != null && project.startsWith("http://")
                || project != null && project.startsWith("https://");
    }

    private void showError(Throwable ex) {
        String msg;
        if (StringUtil.isEmpty(ex.getMessage())) {
            msg = ex.getClass().getName();
        } else {
            msg = ex.getMessage();
        }
        JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), msg,
                OStrings.getString("STARTUP_ERRORBOX_TITLE"), JOptionPane.ERROR_MESSAGE);
    }

}
