/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
               2025 Hiroshi Miura
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

import com.vlsolutions.swing.docking.DockingDesktop;
import org.apache.commons.lang3.StringUtils;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.RealProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.core.statistics.CalcStandardStatistics;
import org.omegat.core.statistics.StatOutputFormat;
import org.omegat.core.statistics.StatsResult;
import org.omegat.core.tagvalidation.ErrorReport;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.main.ProjectUICommands;
import org.omegat.gui.scripting.ConsoleBindings;
import org.omegat.gui.scripting.ScriptItem;
import org.omegat.gui.scripting.ScriptRunner;
import org.omegat.util.FileUtil;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Platform;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.RuntimePreferences;
import org.omegat.util.StringUtil;
import org.omegat.util.TMXWriter2;
import org.omegat.util.gui.OSXIntegration;
import org.omegat.util.gui.UIDesignManager;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.PropertyResourceBundle;

final class StandardCommandLauncher {

    private final Parameters params;

    StandardCommandLauncher(Parameters params) {
        this.params = params;
        if (params.verbose) {
            Log.setConsoleLevel(java.util.logging.Level.INFO);
        }
        showStartUpLogInfo();
    }

    /**
     * Execute a script as PROJECT_CHANGE events. We can't use the regular
     * project listener because the SwingUtilities.invokeLater method used in
     * CoreEvents doesn't stop the project processing in console mode.
     */
    private void executeConsoleScript(IProjectEventListener.PROJECT_CHANGE_TYPE eventType) {
        if (params.scriptName != null) {
            File script = new File(params.scriptName);
            Log.logInfoRB("CONSOLE_EXECUTE_SCRIPT", script, eventType);
            if (script.isFile()) {
                HashMap<String, Object> binding = new HashMap<>();
                binding.put("eventType", eventType);

                ConsoleBindings consoleBindigs = new ConsoleBindings();
                binding.put(ScriptRunner.VAR_CONSOLE, consoleBindigs);
                binding.put(ScriptRunner.VAR_GLOSSARY, consoleBindigs);
                binding.put(ScriptRunner.VAR_EDITOR, consoleBindigs);

                try {
                    String result = ScriptRunner.executeScript(new ScriptItem(script), binding);
                    Log.log(result);
                } catch (Exception ex) {
                    Log.log(ex);
                }
            } else {
                Log.logInfoRB("SCW_SCRIPT_LOAD_ERROR", "the script is not a file");
            }
        }
    }

    /**
     * Validates tags according to command line specs:
     * <code>--tag-validation=[abort|warn]</code>
     * <p>
     * On abort, the program is aborted when tag validation finds errors. On
     * warning, the errors are printed, but the program continues. In all other
     * cases, no tag validation is done.
     */
    private void validateTagsConsoleMode() {
        List<ErrorReport> stes;
        if ("abort".equalsIgnoreCase(params.tagValidation)) {
            System.out.println(OStrings.getString("CONSOLE_VALIDATING_TAGS"));
            stes = Core.getTagValidation().listInvalidTags();
            if (!stes.isEmpty()) {
                Core.getTagValidation().logTagValidationErrors(stes);
                System.out.println(OStrings.getString("CONSOLE_TAGVALIDATION_FAIL"));
                System.out.println(OStrings.getString("CONSOLE_TAGVALIDATION_ABORT"));
                System.exit(1);
            }
        } else if ("warn".equalsIgnoreCase(params.tagValidation)) {
            System.out.println(OStrings.getString("CONSOLE_VALIDATING_TAGS"));
            stes = Core.getTagValidation().listInvalidTags();
            if (!stes.isEmpty()) {
                Core.getTagValidation().logTagValidationErrors(stes);
                System.out.println(OStrings.getString("CONSOLE_TAGVALIDATION_FAIL"));
            }
        }
    }

    /**
     * Execute in console mode for translate.
     */
    int runCreatePseudoTranslateTMX() throws Exception {
        Log.logInfoRB("CONSOLE_PSEUDO_TRANSLATION_MODE");

        System.out.println(OStrings.getString("CONSOLE_INITIALIZING"));
        Core.initializeConsole();

        RealProject p = selectProjectConsoleMode(true);

        validateTagsConsoleMode();

        System.out.println(OStrings.getString("CONSOLE_CREATE_PSEUDOTMX"));

        ProjectProperties config = p.getProjectProperties();
        List<SourceTextEntry> entries = p.getAllEntries();
        String pseudoTranslateTMXFilename = params.pseudoTranslateTmxPath;
        String pseudoTranslateType = params.pseudoTranslateTypeName;

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

    int runConsoleAlign() throws Exception {
        Log.logInfoRB("CONSOLE_ALIGNMENT_MODE");

        if (params.projectLocation == null) {
            System.out.println(OStrings.getString("PP_ERROR_UNABLE_TO_READ_PROJECT_FILE"));
            return 1;
        }

        if (params.alignDirPath == null) {
            System.out.println(OStrings.getString("CONSOLE_TRANSLATED_FILES_LOC_UNDEFINED"));
            return 1;
        }

        System.out.println(OStrings.getString("CONSOLE_INITIALIZING"));
        Core.initializeConsole();
        RealProject p = selectProjectConsoleMode(true);

        validateTagsConsoleMode();

        System.out
                .println(StringUtil.format(OStrings.getString("CONSOLE_ALIGN_AGAINST"), params.alignDirPath));

        String tmxFile = p.getProjectProperties().getProjectInternal() + "align.tmx";
        ProjectProperties config = p.getProjectProperties();
        boolean alt = !config.isSupportDefaultTranslations();
        try (TMXWriter2 wr = new TMXWriter2(new File(tmxFile), config.getSourceLanguage(),
                config.getTargetLanguage(), config.isSentenceSegmentingEnabled(), alt, alt)) {
            wr.writeEntries(p.align(config, new File(FileUtil.expandTildeHomeDir(params.alignDirPath))), alt);
        }
        p.closeProject();
        System.out.println(OStrings.getString("CONSOLE_FINISHED"));
        return 0;
    }

    public int runGUIAligner() {
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

    /**
     * creates the project class and adds it to the Core. Loads the project if
     * specified. An exit occurs on error loading the project. This method is
     * for the different console modes, to prevent code duplication.
     *
     * @param loadProject
     *            load the project or not
     * @return the project.
     */
    private RealProject selectProjectConsoleMode(boolean loadProject) {
        System.out.println(OStrings.getString("CONSOLE_LOADING_PROJECT"));

        // check if project okay
        ProjectProperties projectProperties = null;
        try {
            projectProperties = ProjectFileStorage
                    .loadProjectProperties(Paths.get(params.projectLocation).toFile());
            projectProperties.verifyProject();
        } catch (Exception ex) {
            Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
            System.out.println(OStrings.getString("PP_ERROR_UNABLE_TO_READ_PROJECT_FILE"));
            System.exit(1);
        }

        RealProject p = new RealProject(projectProperties);
        Core.setProject(p);
        if (loadProject) {
            p.loadProject(true);
            if (!p.isProjectLoaded()) {
                Core.setProject(new NotLoadedProject());
            } else {
                executeConsoleScript(IProjectEventListener.PROJECT_CHANGE_TYPE.LOAD);
            }

        }
        return p;
    }

    /**
     * Load System properties from a specified .properties file. In order to
     * allow this to reliably change the display language, it must called before
     * any use of {@link Log#log}, thus it logs to {@link System#out}.
     *
     * @param path
     *            to config file
     */
    private void applyConfigFile(String path) {
        if (path == null) {
            return;
        }
        File configFile = new File(FileUtil.expandTildeHomeDir(path));
        if (!configFile.exists()) {
            return;
        }
        System.out.println("Reading config from " + path);
        try (FileInputStream in = new FileInputStream(configFile)) {
            PropertyResourceBundle config = new PropertyResourceBundle(in);
            // Put config properties into System properties and into OmegaT
            // params.
            for (String key : config.keySet()) {
                String value = config.getString(key);
                System.setProperty(key, value);
                System.out.println("Read from config: " + key + "=" + value);
            }
            // Apply language preferences, if present.
            // This must be done with Locale.setDefault(). Merely doing
            // System.setProperty() will not work.
            if (config.containsKey("user.language")) {
                String userLanguage = config.getString("user.language");
                Locale userLocale = config.containsKey("user.country")
                        ? new Locale(userLanguage, config.getString("user.country"))
                        : new Locale(userLanguage);
                Locale.setDefault(userLocale);
            }
        } catch (FileNotFoundException exception) {
            System.err.println("Config file not found: " + path);
        } catch (IOException exception) {
            System.err.println("Error while reading config file: " + path);
        }
    }

    /**
     * Execute in console mode for translate.
     */
    int runConsoleTranslate() {
        Log.logInfoRB("STARTUP_CONSOLE_TRANSLATION_MODE");

        if (params.noTeam) {
            RuntimePreferences.setNoTeam();
        }

        System.out.println(OStrings.getString("CONSOLE_INITIALIZING"));
        Core.initializeConsole();

        RealProject p = selectProjectConsoleMode(true);

        validateTagsConsoleMode();

        System.out.println(OStrings.getString("CONSOLE_TRANSLATING"));

        try {
            String sourceMask = params.sourcePattern;
            p.compileProject(Objects.requireNonNullElse(sourceMask, ".*"), false);
        } catch (Exception ex) {
            Log.logErrorRB(ex, "CT_ERROR_COMPILING_PROJECT");
            return 1;
        }

        // Called *after* executing post processing command (unlike the
        // regular PROJECT_CHANGE_TYPE.COMPILE)
        executeConsoleScript(IProjectEventListener.PROJECT_CHANGE_TYPE.COMPILE);

        p.closeProject();
        executeConsoleScript(IProjectEventListener.PROJECT_CHANGE_TYPE.CLOSE);
        System.out.println(OStrings.getString("CONSOLE_FINISHED"));

        return 0;
    }

    /**
     * Displays or writes project statistics.
     * <p>
     * takes two optional arguments
     * <code>[--output-file=(file path) [--stats-type=[XML|JSON|TEXT]]]</code>
     * when omitted, display stats text(localized). When file I/O error
     * occurred, especially when parent directory does not exist warns it and
     * return 1.
     */
    int runConsoleStats() throws Exception {
        Log.logInfoRB("STARTUP_CONSOLE_STATS_MODE");

        Core.initializeConsole();

        RealProject p = selectProjectConsoleMode(true);
        StatsResult projectStats = CalcStandardStatistics.buildProjectStats(p);

        if (params.statsOutput == null) {
            // no output file specified, print to console.
            System.out.println(projectStats.getTextData());
            p.closeProject();
            return 0;
        }

        String outputFilename = params.statsOutput;
        StatOutputFormat statsMode;
        if (params.statsType == null) {
            // when no stats type specified, try to detect from file extension,
            // otherwise XML.
            if (outputFilename.toLowerCase().endsWith(StatOutputFormat.JSON.getFileExtension())) {
                statsMode = StatOutputFormat.JSON;
            } else if (outputFilename.toLowerCase().endsWith(StatOutputFormat.XML.getFileExtension())) {
                statsMode = StatOutputFormat.XML;
            } else if (outputFilename.toLowerCase().endsWith(StatOutputFormat.TEXT.getFileExtension())) {
                statsMode = StatOutputFormat.TEXT;
            } else {
                statsMode = StatOutputFormat.XML;
            }
        } else if (StatOutputFormat.JSON.toString().equals(params.statsType)) {
            statsMode = StatOutputFormat.JSON;
        } else if (StatOutputFormat.XML.toString().equals(params.statsType)) {
            statsMode = StatOutputFormat.XML;
        } else if (StatOutputFormat.TEXT.toString().equals(params.statsType)) {
            statsMode = StatOutputFormat.TEXT;
        } else {
            statsMode = StatOutputFormat.XML;
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(
                Paths.get(FileUtil.expandTildeHomeDir(outputFilename)), StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE), StandardCharsets.UTF_8)) {
            switch (statsMode) {
            case TEXT:
                writer.write(projectStats.getTextData());
                break;
            case JSON:
                writer.write(projectStats.getJsonData());
                break;
            case XML:
                writer.write(projectStats.getXmlData());
                break;
            default:
                Log.logWarningRB("CONSOLE_STATS_WARNING_TYPE");
                break;
            }
        } catch (NoSuchFileException nsfe) {
            Log.logErrorRB("CONSOLE_STATS_FILE_OPEN_ERROR");
            return 1;
        } finally {
            p.closeProject();
        }
        return 0;
    }

    private void showStartUpLogInfo() {
        // initialize logging backend and loading configuration.
        Log.logInfoRB("STARTUP_LOGGING_INFO", StringUtils.repeat('=', 120), OStrings.getNameAndVersion(),
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()).format(ZonedDateTime.now()),
                ZoneId.systemDefault().getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                Locale.getDefault().toLanguageTag());
        Log.logInfoRB("LOG_STARTUP_INFO", System.getProperty("java.vendor"),
                System.getProperty("java.version"), System.getProperty("java.home"));

        Log.logInfoRB("STARTUP_GUI_DOCKING_FRAMEWORK", DockingDesktop.getDockingFrameworkVersion());
    }

    /**
     * Execute standard GUI.
     */
    int runGUI() {
        if (params.noTeam) {
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
        try {
            if (cls.getName().equals("sun.awt.X11.XToolkit")) {
                Field field = cls.getDeclaredField("awtAppClassName");
                if (field.trySetAccessible()) {
                    field.set(toolkit, "OmegaT");
                }
            }
        } catch (Exception ignored) {
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

        if (params.alternateFilenameFrom != null) {
            RuntimePreferences.setAlternateFilenames(params.alternateFilenameFrom, params.alternateFilenameTo);
        }

        CoreEvents.fireApplicationStartup();

        SwingUtilities.invokeLater(() -> {
            // setVisible can't be executed directly, because we need to
            // call all application startup listeners for initialize UI
            Core.getMainWindow().getApplicationFrame().setVisible(true);
            //
            if (isProjectRemote(params.projectLocation)) {
                ProjectUICommands.projectRemote(params.projectLocation);
            } else if (params.projectLocation != null) {
                File targetDir = Paths.get(params.projectLocation).toFile();
                File targetFile = Paths.get(params.projectLocation).resolve("omegat.project").toFile();
                if (targetDir.isDirectory() && targetFile.exists()) {
                    ProjectUICommands.projectOpen(targetDir);
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
