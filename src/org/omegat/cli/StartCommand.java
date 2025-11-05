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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IProjectEventListener;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Start sub-command entry.
 */
@CommandLine.Command(name = "start", resourceBundle = "org.omegat.cli.Parameters")
public class StartCommand implements Callable<Integer> {

    @CommandLine.ParentCommand
    @Nullable LegacyParameters legacyParams;

    @CommandLine.Mixin
    @Nullable CommonParameters params;

    @CommandLine.Option(names = { "-h", "--help" }, usageHelp = true)
    boolean usageHelpRequested = false;

    @CommandLine.Parameters(index = "0", paramLabel = "<project>", defaultValue = CommandLine.Option.NULL_VALUE)
    @Nullable String project;

    @Override
    public Integer call() {
        if (legacyParams == null || params == null) {
            return 1;
        }
        legacyParams.initialize();
        params.setProjectLocation(Objects.requireNonNullElse(project, "."));
        return runGUI();
    }

    /**
     * Execute standard GUI.
     */
    int runGUI() {
        CommandCommon.showStartUpLogInfo();
        if (params != null) {
            CommandCommon.logLevelInitialize(params);
            if (params.tokenizerSource != null) {
                RuntimePreferences.setTokenizerSource(params.tokenizerSource);
            }
            if (params.tokenizerTarget != null) {
                RuntimePreferences.setTokenizerTarget(params.tokenizerTarget);
            }
        }

        CommandCommon.initializeApp();

        if ((params != null && !params.team) || (legacyParams != null && legacyParams.noTeam)) {
            RuntimePreferences.setNoTeam();
        }

        UIManager.put("ClassLoader", PluginUtils.getClassLoader(PluginUtils.PluginType.THEME));

        // macOS-specific - they must be set BEFORE any GUI calls
        if (Platform.isMacOSX()) {
            OSXIntegration.init();
        }

        // Set X11 application class name to make some desktop user interfaces
        // (like Gnome Shell) recognize OmegaT
        if (Platform.isUnixLike()) {
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

        if (params != null && params.alternateFilenameFrom != null && params.alternateFilenameTo != null) {
            RuntimePreferences.setAlternateFilenames(params.alternateFilenameFrom, params.alternateFilenameTo);
        }

        CoreEvents.fireApplicationStartup();

        SwingUtilities.invokeLater(() -> {
            // setVisible can't be executed directly, because we need to
            // call all application startup listeners for initialize UI
            Core.getMainWindow().getApplicationFrame().setVisible(true);
            if (params != null && params.projectLocation != null) {
                if (isProjectRemote(params.projectLocation)) {
                    ProjectUICommands.projectRemote(params.projectLocation);
                } else {
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

    private boolean isProjectRemote(@NotNull String projectPath) {
        return projectPath.startsWith("http://") || projectPath.startsWith("https://");
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

    /**
     * Execute a script as PROJECT_CHANGE events. We can't use the regular
     * project listener because the SwingUtilities.invokeLater method used in
     * CoreEvents doesn't stop the project processing in console mode.
     */
    private void executeConsoleScript(IProjectEventListener.PROJECT_CHANGE_TYPE eventType) {
        if (params.scriptName == null) {
            return;
        }
        File script = new File(params.scriptName);
        Log.logInfoRB("CONSOLE_EXECUTE_SCRIPT", script, eventType);
        if (script.isFile()) {
            try {
                ClassLoader cl = PluginUtils.getClassLoader(PluginUtils.PluginType.MISCELLANEOUS);
                if (cl == null) {
                    Log.logErrorRB("SCW_SCRIPT_LOAD_ERROR", "the plugin classloader is null");
                    return;
                }
                Class<?> scriptingClass = cl.loadClass("org.omegat.gui.scripting.ScriptingModule");
                Method method = scriptingClass.getMethod("executeConsoleScript",
                        IProjectEventListener.PROJECT_CHANGE_TYPE.class, File.class);
                method.invoke(null, eventType, script);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                     | InvocationTargetException e) {
                Log.log(e);
            }
        } else {
            Log.logInfoRB("SCW_SCRIPT_LOAD_ERROR", "the script is not a file");
        }

    }

}
